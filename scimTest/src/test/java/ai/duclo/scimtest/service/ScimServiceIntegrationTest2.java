package ai.duclo.scimtest.service;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.time.Duration;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
// First Server Mock 설정 (기본 인스턴스)
@AutoConfigureWireMock(
        ports = 0, // 랜덤 포트
        properties = {
                // First Server URL을 WireMock 포트로 오버라이드
                "scim.downstream.first-server-url=http://localhost:${wiremock.server.port}",
                // First Server CB 설정: 5번 중 50% 실패 시 OPEN (테스트용 짧은 설정)
                "resilience4j.circuitbreaker.instances.firstServer.sliding-window-size=5",
                "resilience4j.circuitbreaker.instances.firstServer.failure-rate-threshold=50",
                "resilience4j.circuitbreaker.instances.firstServer.wait-duration-in-open-state=10ms"
        }
)
// Second Server Mock 설정 (secondServerMock 이라는 이름으로 인스턴스 분리)
@AutoConfigureWireMock(
        names = "secondServerMock",
        ports = 0, // 랜덤 포트
        properties = {
                // Second Server URL을 두 번째 WireMock 포트로 오버라이드
                "scim.downstream.second-server-url=http://localhost:${wiremock.secondServerMock.port}",
                // Retry 설정: 최대 3회 재시도 (테스트용 짧은 설정)
                "resilience4j.retry.instances.serverApiRetry.max-attempts=3",
                "resilience4j.retry.instances.serverApiRetry.wait-duration=10ms"
        }
)
public class ScimServiceIntegrationTest2 {

    @Autowired
    private ScimService scimService;

    @Autowired
    private CircuitBreakerRegistry circuitBreakerRegistry;

    // 첫 번째 서버 WireMock 인스턴스 (Spring이 자동으로 주입)
    @Autowired
    private WireMockServer wireMockServer;

    // 두 번째 서버 WireMock 인스턴스 (이름을 지정하여 주입)
    @Autowired
    @WireMockServer.InjectWireMock("secondServerMock")
    private WireMockServer secondServerMock;

    private static final User TEST_USER = new User("test-user-id", "testUser", "Test", "User");
    private static final String SCIM_USER_PATH = "/users";

    // 테스트에 필요한 User Record (실제 코드의 User 클래스에 맞게 수정 필요)
    record User(String id, String userName, String firstName, String lastName) {}

    @BeforeEach
    void setUp() {
        // 테스트 간 간섭을 막기 위해 CircuitBreaker 상태를 초기화
        circuitBreakerRegistry.circuitBreaker("firstServer").reset();
        circuitBreakerRegistry.circuitBreaker("secondServer").reset();

        // WireMock Stub 초기화 (Very Important!)
        wireMockServer.resetAll();
        secondServerMock.resetAll();
    }

    // --- 시나리오 1: Retry 로직 테스트 ---

    @Test
    @DisplayName("Second Server: 2회 실패 후 3번째 성공 시, 재시도 횟수(3)만큼 호출 후 성공해야 한다.")
    void processUserCreation_secondServerRetrySuccess() {
        // 1. First Server Stub: 성공 (생성 성공)
        wireMockServer.stubFor(post(urlEqualTo(SCIM_USER_PATH))
                .willReturn(aResponse().withStatus(201).withBody("{\"id\": \"user-1\", \"userName\": \"testUser\"}")));

        // 2. Second Server Stub: 2번은 500 에러, 3번째는 201 성공 응답 (Sequence 사용)
        secondServerMock.stubFor(post(urlEqualTo(SCIM_USER_PATH))
                .inScenario("Retry Scenario")
                .whenScenarioStateIs(WireMock.STARTED)
                .willReturn(aResponse().withStatus(500))
                .willSetStateTo("Retry 1 Failed"));

        secondServerMock.stubFor(post(urlEqualTo(SCIM_USER_PATH))
                .inScenario("Retry Scenario")
                .whenScenarioStateIs("Retry 1 Failed")
                .willReturn(aResponse().withStatus(500))
                .willSetStateTo("Retry 2 Failed"));

        // 3번째 시도 (State: Retry 2 Failed)에 성공 응답
        secondServerMock.stubFor(post(urlEqualTo(SCIM_USER_PATH))
                .inScenario("Retry Scenario")
                .whenScenarioStateIs("Retry 2 Failed")
                .willReturn(aResponse().withStatus(201).withBody("{\"id\": \"user-2\", \"userName\": \"testUser\"}"))
                .willSetStateTo(Scenario.STARTED)); // 다음 테스트를 위해 초기화

        // 3. 테스트 실행 및 검증: 최종적으로 성공해야 함
        StepVerifier.create(scimService.processUserCreation(TEST_USER))
                .expectNextMatches(user -> user.id().equals("user-2"))
                .verifyComplete();

        // 4. Second Server 호출 횟수 검증: 정확히 3번의 POST 요청이 갔는지 확인
        secondServerMock.verify(3, postRequestedFor(urlEqualTo(SCIM_USER_PATH)));
    }

    // --- 시나리오 2: CircuitBreaker 동작 테스트 (CLOSE -> OPEN) ---

    @Test
    @DisplayName("First Server: 실패율 임계값 초과 시, CircuitBreaker가 OPEN 상태로 전환되어야 한다.")
    void processUserCreation_firstServerCircuitBreakerOpens() {
        // 1. First Server Stub: 100% 실패 응답 (500 Internal Server Error)
        wireMockServer.stubFor(post(urlEqualTo(SCIM_USER_PATH))
                .willReturn(aResponse().withStatus(500)));

        // 2. 실패 임계값(5회 호출 중 3회 실패)을 초과하도록 5번 요청을 보냄
        Flux.range(1, 5)
                .flatMap(i -> scimService.processUserCreation(TEST_USER).onErrorResume(e -> Mono.empty()))
                .blockLast(Duration.ofSeconds(5));

        // 3. CircuitBreaker 상태 검증: OPEN 상태로 전환되었는지 확인
        CircuitBreaker firstServerCB = circuitBreakerRegistry.circuitBreaker("firstServer");
        assertThat(firstServerCB.getState()).isEqualTo(CircuitBreaker.State.OPEN);

        // 4. OPEN 상태에서의 요청 검증: 실제 서버로 전달되지 않고 CallNotPermittedException 발생 확인
        StepVerifier.create(scimService.processUserCreation(TEST_USER))
                .expectError(io.github.resilience4j.circuitbreaker.CallNotPermittedException.class)
                .verify(Duration.ofMillis(100)); // 지연 없이 즉시 실패해야 함

        // 5. WireMock 호출 횟수 검증: 5번만 호출되었는지 확인 (6번째부터는 차단됨)
        wireMockServer.verify(5, postRequestedFor(urlEqualTo(SCIM_USER_PATH)));
    }

    // --- 시나리오 3: 롤백 로직 테스트 ---

    @Test
    @DisplayName("Second Server 실패 시: First Server에 롤백 (DELETE) 요청이 정확히 전송되어야 한다.")
    void processUserCreation_secondServerFailure_shouldTriggerRollback() {
        final String createdUserId = "rollback-user-123";

        // 1. First Server Stub: 성공 응답 (생성 성공)
        wireMockServer.stubFor(post(urlEqualTo(SCIM_USER_PATH))
                .willReturn(aResponse().withStatus(201).withBody(
                        "{\"id\": \"" + createdUserId + "\", \"userName\": \"testUser\"}"
                )));

        // 2. Second Server Stub: 재시도 횟수(3회) 모두 500 에러로 실패
        secondServerMock.stubFor(post(urlEqualTo(SCIM_USER_PATH))
                .willReturn(aResponse().withStatus(500)));

        // 3. First Server Rollback Stub: DELETE 요청 성공 (롤백 성공)
        wireMockServer.stubFor(delete(urlEqualTo(SCIM_USER_PATH + "/" + createdUserId))
                .willReturn(aResponse().withStatus(204))); // 204 No Content

        // 4. 테스트 실행 및 검증: 최종적으로 예외가 발생해야 합니다.
        StepVerifier.create(scimService.processUserCreation(TEST_USER))
                .expectErrorSatisfies(e -> {
                    // Second Server의 최종 실패 예외로 종료되는지 확인
                    assertThat(e.getMessage()).contains("Second server failed with status 500");
                })
                .verify(Duration.ofSeconds(5));

        // 5. WireMock 검증:
        // - Second Server에 POST 요청이 Retry 횟수(3회)만큼 전송되었는지 확인
        secondServerMock.verify(3, postRequestedFor(urlEqualTo(SCIM_USER_PATH)));

        // - First Server에 롤백을 위한 DELETE 요청이 1번 전송되었는지 확인
        wireMockServer.verify(1, deleteRequestedFor(urlEqualTo(SCIM_USER_PATH + "/" + createdUserId)));
    }
}

