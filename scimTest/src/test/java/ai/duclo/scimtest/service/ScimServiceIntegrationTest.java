package ai.duclo.scimtest.service;

import com.github.tomakehurst.wiremock.WireMockServer;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import java.time.Duration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

//@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
//// 테스트 시 WireMock 서버 2개를 각각 다른 포트로 띄우고 URL 주입
//@AutoConfigureWireMock(
//        // first-server-url에 주입할 WireMock 설정
//        ports = 0,
//        properties = {
//                "scim.downstream.first-server-url=http://localhost:${wiremock.server.port}",
//                "resilience4j.circuitbreaker.instances.firstServer.sliding-window-size=5",
//                "resilience4j.circuitbreaker.instances.firstServer.failure-rate-threshold=50",
//                "resilience4j.circuitbreaker.instances.firstServer.wait-duration-in-open-state=10ms" // 테스트를 위해 짧게 설정
//        }
//)
//// second-server-url에 주입할 두 번째 WireMock 인스턴스 설정
//@AutoConfigureWireMock(
//        names = "secondServerMock",
//        ports = 0,
//        properties = {
//                "scim.downstream.second-server-url=http://localhost:${wiremock.secondServerMock.port}",
//                "resilience4j.circuitbreaker.instances.secondServer.sliding-window-size=5",
//                "resilience4j.circuitbreaker.instances.secondServer.failure-rate-threshold=50",
//                "resilience4j.circuitbreaker.instances.secondServer.wait-duration-in-open-state=10ms",
//                "resilience4j.retry.instances.serverApiRetry.max-attempts=3", // 재시도 횟수 설정
//                "resilience4j.retry.instances.serverApiRetry.wait-duration=10ms" // 테스트를 위해 짧게 설정
//        }
//)
public class ScimServiceIntegrationTest {
//    @Autowired
//    private ScimService scimService;
//
//    @Autowired
//    private CircuitBreakerRegistry circuitBreakerRegistry;
//
//    // 첫 번째 서버 WireMock 인스턴스 (Default)
//    @Autowired
//    private WireMockServer wireMockServer;
//
//    // 두 번째 서버 WireMock 인스턴스 (이름을 지정하여 주입)
//    @InjectWireMock("secondServerMock")
//    private WireMockServer secondServerMock;
//
//    private User testUser;
//
//    @BeforeEach
//    void setUp() {
//        // 테스트 전 CircuitBreaker 상태를 'CLOSED'로 초기화 (테스트 간 간섭 방지)
//        circuitBreakerRegistry.circuitBreaker("firstServer").reset();
//        circuitBreakerRegistry.circuitBreaker("secondServer").reset();
//
//        // WireMock Stub 초기화
//        wireMockServer.resetAll();
//        secondServerMock.resetAll();
//
//        // 테스트에 사용할 샘플 User 객체
//        testUser = new User("test-user-id", "testUser", "Test", "User");
//    }
//
//    // User 클래스는 예시이며, 실제 코드에 맞게 정의되어 있어야 합니다.
//    static record User(String id, String userName, String firstName, String lastName) {}
//
//
//    @Test
//    @DisplayName("First Server: 2회 실패 후 3번째 성공 시, 최종적으로 성공해야 하며, 재시도 횟수만큼 호출되어야 한다.")
//    void createFirstServerUser_retrySuccess() {
//        // 1. WireMock Stub 설정: 2번은 500 에러, 3번째는 201 성공 응답
//        // 횟수 제어를 위해 sequence를 사용합니다.
//        wireMockServer.stubFor(post(urlEqualTo("/users"))
//                .inScenario("Retry Success") // 시나리오 이름
//                .whenNotMatched(aResponse().withStatus(500)) // 기본 실패
//                .willReturn(aResponse().withStatus(201).withBody(
//                        "{\"id\": \"user-1\", \"userName\": \"testUser\"}"
//                ))
//                .willSetStateTo("Success State")); // 3번째 시도부터 성공
//
//        // 2. Second Server Stub 설정 (First Server가 성공하면 Second Server도 성공해야 하므로)
//        secondServerMock.stubFor(post(urlEqualTo("/users"))
//                .willReturn(aResponse().withStatus(201).withBody(
//                        "{\"id\": \"user-1-final\", \"userName\": \"testUser\"}"
//                )));
//
//        // 3. 테스트 실행 및 검증
//        StepVerifier.create(scimService.processUserCreation(testUser))
//                .expectNextMatches(user -> user.id().equals("user-1-final"))
//                .verifyComplete();
//
//        // 4. WireMock 호출 횟수 검증: 첫 번째 서버에 3번의 POST 요청이 갔는지 확인
//        wireMockServer.verify(3, postRequestedFor(urlEqualTo("/users")));
//
//        // 5. CircuitBreaker 상태 검증: 상태가 OPEN으로 바뀌지 않고 CLOSED를 유지하는지 확인
//        CircuitBreaker firstServerCB = circuitBreakerRegistry.circuitBreaker("firstServer");
//        assertThat(firstServerCB.getState()).isEqualTo(CircuitBreaker.State.CLOSED);
//    }
//
//    @Test
//    @DisplayName("CircuitBreaker: 5회 중 3회 이상 실패 시, CircuitBreaker가 OPEN 상태로 전환되어야 한다.")
//    void createFirstServerUser_circuitBreakerOpens() {
//        // 1. WireMock Stub 설정: 100% 실패 (500 Internal Server Error)
//        // CircuitBreaker 설정을 5회 호출 중 50% 실패 시 OPEN으로 가정합니다.
//        wireMockServer.stubFor(post(urlEqualTo("/users"))
//                .willReturn(aResponse().withStatus(500)));
//
//        // 2. 실패 임계값(Threshold)을 초과하도록 충분히 많은 요청을 보냄 (예: 5회)
//        // Mono.onErrorResume을 사용하여 예외 발생 시 스트림이 끊어지지 않도록 처리
//        Mono<User> failureMono = scimService.processUserCreation(testUser)
//                .onErrorResume(e -> Mono.empty());
//
//        // 5회 호출을 실행
//        Flux.range(1, 5)
//                .flatMap(i -> failureMono)
//                .blockLast(Duration.ofSeconds(5)); // 동기적으로 기다림 (테스트용)
//
//        // 3. CircuitBreaker 상태 검증
//        CircuitBreaker firstServerCB = circuitBreakerRegistry.circuitBreaker("firstServer");
//        assertThat(firstServerCB.getState()).isEqualTo(CircuitBreaker.State.OPEN);
//
//        // 4. OPEN 상태에서의 요청 검증: 즉시 CallNotPermittedException 발생 확인
//        // 실제 서버로 요청이 전달되지 않고 예외가 발생해야 합니다.
//        StepVerifier.create(scimService.processUserCreation(testUser))
//                .expectError(io.github.resilience4j.circuitbreaker.CallNotPermittedException.class)
//                .verify(Duration.ofMillis(100)); // 매우 빠르게 실패해야 합니다.
//
//        // 5. WireMock 호출 횟수 검증: 5번만 호출되었는지 확인 (6번째부터는 차단됨)
//        wireMockServer.verify(5, postRequestedFor(urlEqualTo("/users")));
//    }
//
//
//    @Test
//    @DisplayName("Second Server 실패 시: First Server에 롤백 (DELETE) 요청이 전송되어야 한다.")
//    void processUserCreation_secondServerFailure_shouldRollbackFirstServer() {
//        final String createdUserId = "rollback-user-123";
//
//        // 1. First Server Stub 설정: 성공 응답 (생성 성공)
//        wireMockServer.stubFor(post(urlEqualTo("/users"))
//                .willReturn(aResponse().withStatus(201).withBody(
//                        "{\"id\": \"" + createdUserId + "\", \"userName\": \"testUser\"}"
//                )));
//
//        // 2. Second Server Stub 설정: 재시도 횟수(3회) 모두 500 에러로 실패
//        secondServerMock.stubFor(post(urlEqualTo("/users"))
//                .willReturn(aResponse().withStatus(500)));
//
//        // 3. First Server Rollback Stub 설정: DELETE 요청 성공 (롤백 성공)
//        wireMockServer.stubFor(delete(urlEqualTo("/users/" + createdUserId))
//                .willReturn(aResponse().withStatus(204)));
//
//        // 4. 테스트 실행 및 검증: 최종적으로 예외가 발생해야 합니다.
//        StepVerifier.create(scimService.processUserCreation(testUser))
//                .expectErrorSatisfies(e -> {
//                    // RuntimeException("Second server failed with status 500") 예외로 종료되는지 확인
//                    assertThat(e).isInstanceOf(RuntimeException.class);
//                    assertThat(e.getMessage()).contains("Second server failed with status 500");
//                })
//                .verify(Duration.ofSeconds(5));
//
//        // 5. WireMock 검증:
//        // - Second Server에 POST 요청이 Retry 횟수(3회)만큼 전송되었는지 확인
//        secondServerMock.verify(3, postRequestedFor(urlEqualTo("/users")));
//
//        // - First Server에 롤백을 위한 DELETE 요청이 1번 전송되었는지 확인
//        wireMockServer.verify(1, deleteRequestedFor(urlEqualTo("/users/" + createdUserId)));
//    }
}
