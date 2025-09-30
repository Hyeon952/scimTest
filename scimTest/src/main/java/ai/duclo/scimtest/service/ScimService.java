package ai.duclo.scimtest.service;

import ai.duclo.scimtest.model.internal.AccountRequestV2;
import ai.duclo.scimtest.model.scim.User;
import ai.duclo.scimtest.model.scim.UserResponseDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryRegistry;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpResponse;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class ScimService {

    private final CloseableHttpClient firstServerClient;
    private final CloseableHttpClient secondServerClient;

    @Value("${scim.downstream.first-server-url}")
    String firstServerUrl;
    @Value("${scim.downstream.second-server-url}")
    String secondServerUrl;

    private final ObjectMapper objectMapper = new ObjectMapper();


    // Circuitbreaker 및 재시도 인스턴스
    private final CircuitBreaker firstServerCircuitBreaker;
    private final CircuitBreaker secondServerCircuitBreaker;
    private final Retry serverApiRetry;


    public ScimService(CircuitBreakerRegistry circuitBreakerRegistry, // Resilience4j 주입
                       RetryRegistry retryRegistry // Resilience4j 주입
                       ) {
        this.firstServerClient = HttpClientBuilder.create().build();
        this.secondServerClient = HttpClientBuilder.create().build();
        // 주입받은 Registry를 사용하여 Circuitbreaker와 재시도 인스턴스를 가져옵니다.
        this.firstServerCircuitBreaker = circuitBreakerRegistry.circuitBreaker("firstServer");
        this.secondServerCircuitBreaker = circuitBreakerRegistry.circuitBreaker("secondServer");
        this.serverApiRetry = retryRegistry.retry("serverApiRetry");
    }

    public User processUserCreation(User user, String idpId) throws Exception {
        log.info("Starting user creation process for user: {}, idpId : {}", user.getUserName(), idpId);
        try {
             if(createSecondServerUser(AccountRequestV2.of(user), idpId)){
                 log.info("User creation successful for user: {}", user.getUserName());
                 return UserResponseDTO.create(user);
             } else {
                 throw new RuntimeException("Second server user creation failed.");
             }
        } catch (Exception e) {
            log.error("User creation failed, full stack trace: {}", e.getMessage(), e);
            throw e;
        }
    }

    public Boolean createFirstServerUser(User user) throws Exception {
        log.info("Calling first server for user: {}", user.getUserName());
        HttpPost post = new HttpPost(firstServerUrl + "/users");
        post.setEntity(new StringEntity(objectMapper.writeValueAsString(AccountRequestV2.of(user)), ContentType.APPLICATION_JSON));
        return firstServerClient.execute(post, response -> {
            try {
                return handleUserResponse(response);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }

    public Boolean createSecondServerUser(AccountRequestV2 accountRequestV2, String idpId) throws Exception {
        log.info("Calling second server for user: {}, {}", accountRequestV2.getUserName(), idpId);
        HttpPost post = new HttpPost(secondServerUrl + "/partner/internal/scim/" + idpId + "/accounts");
        post.setEntity(new StringEntity(objectMapper.writeValueAsString(accountRequestV2), ContentType.APPLICATION_JSON));
        return secondServerClient.execute(post, response -> {
            try {
                return handleUserResponse(response);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }

    public void rollbackFirstServer(String userId) {
        log.warn("Sending rollback command to first server for user: {}", userId);
        HttpDelete delete = new HttpDelete(firstServerUrl + "/users/" + userId);
        try {
            firstServerClient.execute(delete, (ResponseHandler<Void>) response -> {
                int status = response.getStatusLine().getStatusCode();
                if (status >= 400) {
                    log.error("Rollback failed!");
                    throw new RuntimeException("Rollback failed!");
                }
                return null;
            });
        } catch (Exception e) {
            log.error("Failed to rollback first server. Manual intervention may be required.", e);
        }
    }

    public Boolean processUserUpdate(String id, User user) throws Exception {
        log.info("Starting user update process for user id: {}", id);
        Boolean updatedUser = updateFirstServerUser(id, user);
        if(updatedUser) {
            return updateSecondServerUser(user);
        } else {
            throw new RuntimeException("First server update failed.");
        }
    }

    public Boolean updateFirstServerUser(String id, User user) throws Exception {
        log.debug("Calling first server for user update id: {}", id);
        HttpPut put = new HttpPut(firstServerUrl + "/users/" + id);
        put.setEntity(new StringEntity(objectMapper.writeValueAsString(user), ContentType.APPLICATION_JSON));
        return firstServerClient.execute(put, response -> {
            try {
                return handleUserResponse(response);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }

    public Boolean updateSecondServerUser(User firstServerResult) throws Exception {
        log.debug("Calling second server for user update: {}", firstServerResult.getUserName());
        HttpPut put = new HttpPut(secondServerUrl + "/users/" + firstServerResult.getUserName());
        put.setEntity(new StringEntity(objectMapper.writeValueAsString(firstServerResult), ContentType.APPLICATION_JSON));
        try {
            return secondServerClient.execute(put, response -> {
                try {
                    return handleUserResponse(response);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            });
        } catch (Exception e) {
            log.error("Second server update failed, attempting rollback on first server: {}", e.getMessage());
            rollbackFirstServer(firstServerResult.getUserName());
            throw e;
        }
    }

    public void processUserDeletion(String id) throws Exception {
        log.info("Starting user deletion process for user id: {}", id);
        deleteFirstServerUser(id);
        deleteSecondServerUser(id);
        log.info("User deletion successful for user id: {}", id);
    }

    public void deleteFirstServerUser(String id) throws Exception {
        log.debug("Calling first server for user deletion id: {}", id);
        HttpDelete delete = new HttpDelete(firstServerUrl + "/users/" + id);
        firstServerClient.execute(delete, (ResponseHandler<Void>) response -> {
            int status = response.getStatusLine().getStatusCode();
            if (status >= 400) {
                log.warn("First server deletion failed. This might lead to data inconsistency. Status: {}", status);
                throw new RuntimeException("First server deletion failed.");
            }
            return null;
        });
    }

    public void deleteSecondServerUser(String id) throws Exception {
        log.debug("Calling second server for user deletion id: {}", id);
        HttpDelete delete = new HttpDelete(secondServerUrl + "/users/" + id);
        secondServerClient.execute(delete, (ResponseHandler<Void>) response -> {
            int status = response.getStatusLine().getStatusCode();
            if (status >= 400) {
                log.error("Second server deletion failed. User might still exist in the second system. Status: {}", status);
                // 에러를 발생시키지 않고 종료
            }
            return null;
        });
    }

    // HTTP 응답에서 User 객체 파싱
    private Boolean handleUserResponse(HttpResponse response) throws Exception {
        int status = response.getStatusLine().getStatusCode();
        log.info("Response status: {}", status);
        if(status >= 200 && status < 300) {
        	return true;
        } else if (status >= 400) {
            throw new RuntimeException("Server failed with status " + status);
        } else {
            throw new RuntimeException("Unexpected response status: " + status);
        }
//        return objectMapper.readValue(response.getEntity().getContent(), User.class);
    }

//    public User processUserCreation(User user, String idpId) throws Exception {
//        log.info("Starting user creation process for user: {}, idpId : {}", user.getUserName(), idpId);
//        try {
//            return createSecondServerUser(AccountRequestV2.of(user), idpId);
//        } catch (Exception e) {
//            log.error("User creation failed, full stack trace: {}", e.getMessage(), e);
//            throw e;
//        }
//    }
//
//    private Mono<User> createFirstServerUser(User user) {
//        log.info("Calling first server for user: {}", user.getUserName());
//        return firstServerClient.post()
//                .uri("/users")
//                .bodyValue(AccountRequestV2.of(user))
//                .retrieve()
//                .onStatus(HttpStatus::isError, response -> Mono.error(new RuntimeException("First server failed with status " + response.statusCode())))
//                .bodyToMono(User.class)
//                // Circuitbreaker 적용
//                .transformDeferred(CircuitBreakerOperator.of(firstServerCircuitBreaker))
//                // 재시도 로직 적용
//                .transformDeferred(RetryOperator.of(serverApiRetry));
//    }
//
//    private Mono<User> createSecondServerUser(AccountRequestV2 accountRequestV2, String idpId) {
//        log.info("Calling second server for user: {}, {}", accountRequestV2.getUserName(), idpId);
//        return secondServerClient.post()
//                .uri("/partner/internal/scim/"+idpId+"/accounts")
//                .bodyValue(accountRequestV2)
//                .retrieve()
//                .onStatus(HttpStatus::isError, response -> Mono.error(new RuntimeException("Second server failed with status " + response.statusCode())))
//                .bodyToMono(User.class)
//                // Circuitbreaker 적용
//                .transformDeferred(CircuitBreakerOperator.of(secondServerCircuitBreaker))
//                // 재시도 로직 적용
//                .transformDeferred(RetryOperator.of(serverApiRetry))
//                .onErrorResume(e -> {
//                    log.error("Second server failed, attempting rollback on first server: {}", e.getMessage());
////                    return rollbackFirstServer(firstServerResult.getUserName())
////                            .then(Mono.error(e));
//                    return Mono.error(e);
//                });
//
//    }
//    private Mono<Void> rollbackFirstServer(String userId) {
//        log.warn("Sending rollback command to first server for user: {}", userId);
//        return firstServerClient.delete()
//                .uri("/users/" + userId)
//                .retrieve()
//                .onStatus(HttpStatus::isError, response -> Mono.error(new RuntimeException("Rollback failed!")))
//                .bodyToMono(Void.class)
//                .onErrorResume(e -> {
//                    log.error("Failed to rollback first server. Manual intervention may be required.", e);
//                    return Mono.empty();
//                });
//    }
//
//    public Mono<User> processUserUpdate(String id, User user) {
//        log.info("Starting user update process for user id: {}", id);
//
//        return updateFirstServerUser(id, user)
//                .flatMap(this::updateSecondServerUser)
//                .doOnSuccess(updatedUser -> log.info("User update successful for user: {}", updatedUser.getUserName()))
//                .doOnError(e -> log.error("User update failed, full stack trace: {}", e.getMessage(), e));
//    }
//
//    private Mono<User> updateFirstServerUser(String id, User user) {
//        log.debug("Calling first server for user update id: {}", id);
//        return firstServerClient.put()
//                .uri("/users/" + id)
//                .bodyValue(user)
//                .retrieve()
//                .onStatus(HttpStatus::isError, response -> Mono.error(new RuntimeException("First server update failed with status " + response.statusCode())))
//                .bodyToMono(User.class)
//                .transformDeferred(CircuitBreakerOperator.of(firstServerCircuitBreaker))
//                .transformDeferred(RetryOperator.of(serverApiRetry));
//    }
//
//    private Mono<User> updateSecondServerUser(User firstServerResult) {
//        log.debug("Calling second server for user update: {}", firstServerResult.getUserName());
//        return secondServerClient.put()
//                .uri("/users/" + firstServerResult.getUserName())
//                .bodyValue(firstServerResult)
//                .retrieve()
//                .onStatus(HttpStatus::isError, response -> Mono.error(new RuntimeException("Second server update failed with status " + response.statusCode())))
//                .bodyToMono(User.class)
//                .transformDeferred(CircuitBreakerOperator.of(secondServerCircuitBreaker))
//                .transformDeferred(RetryOperator.of(serverApiRetry))
//                .onErrorResume(e -> {
//                    log.error("Second server update failed, attempting rollback on first server: {}", e.getMessage());
//                    // 업데이트 롤백은 복잡할 수 있으므로, 여기서는 간단하게 삭제 로직을 사용합니다.
//                    // 실제 운영에서는 '이전 상태로 되돌리는' 별도의 API가 필요합니다.
//                    return rollbackFirstServer(firstServerResult.getUserName())
//                            .then(Mono.error(e));
//                });
//    }
//
//    public Mono<Void> processUserDeletion(String id) {
//        log.info("Starting user deletion process for user id: {}", id);
//
//        return deleteFirstServerUser(id)
//                .then(deleteSecondServerUser(id)) // 순차적으로 두 번째 서버의 삭제를 실행
//                .doOnSuccess(v -> log.info("User deletion successful for user id: {}", id))
//                .doOnError(e -> log.error("User deletion failed: {}", e.getMessage(), e));
//    }
//
//    private Mono<Void> deleteFirstServerUser(String id) {
//        log.debug("Calling first server for user deletion id: {}", id);
//        return firstServerClient.delete()
//                .uri("/users/" + id)
//                .retrieve()
//                .onStatus(HttpStatus::isError, response -> {
//                    log.warn("First server deletion failed. This might lead to data inconsistency. Status: {}", response.statusCode());
//                    return Mono.error(new RuntimeException("First server deletion failed."));
//                })
//                .bodyToMono(Void.class)
//                .transformDeferred(CircuitBreakerOperator.of(firstServerCircuitBreaker))
//                .transformDeferred(RetryOperator.of(serverApiRetry));
//    }
//
//    private Mono<Void> deleteSecondServerUser(String id) {
//        log.debug("Calling second server for user deletion id: {}", id);
//        return secondServerClient.delete()
//                .uri("/users/" + id)
//                .retrieve()
//                .onStatus(HttpStatus::isError, response -> {
//                    // 두 번째 서버 실패 시 롤백 로직이 없으므로 경고 로그만 남김
//                    log.error("Second server deletion failed. User might still exist in the second system. Status: {}", response.statusCode());
//                    return Mono.empty(); // 에러를 발생시키지 않고 빈 스트림으로 종료하여 다음 체인에 영향을 주지 않음
//                })
//                .bodyToMono(Void.class)
//                .transformDeferred(CircuitBreakerOperator.of(secondServerCircuitBreaker))
//                .transformDeferred(RetryOperator.of(serverApiRetry));
//    }

//    public Mono<User> processUserCreation(User user) {
//        return firstServerClient.post()
//                .uri("/users")
//                .bodyValue(user)
//                .retrieve()
//                .onStatus(HttpStatusCode::isError, response -> Mono.error(new RuntimeException("First server failed with status " + response.statusCode())))
//                .bodyToMono(User.class)
//                .flatMap(firstServerResult -> secondServerClient.post()
//                        .uri("/users")
//                        .bodyValue(firstServerResult)
//                        .retrieve()
//                        .onStatus(HttpStatusCode::isError, response -> Mono.error(new RuntimeException("Second server failed with status " + response.statusCode())))
//                        .bodyToMono(User.class)
//                        .onErrorResume(e -> {
//                            // 두 번째 서버 실패 시 롤백
//                            System.err.println("Second server operation failed, attempting rollback on first server.");
//                            return rollbackFirstServer(firstServerResult.getId())
//                                    .then(Mono.error(e)); // 원래 에러를 다시 전파
//                        }))
//                .doOnError(e -> System.err.println("Operation failed: " + e.getMessage()));
//    }
//
//    public Mono<Void> processUserDeletion(String id) {
//        // 순차적 삭제: 첫 번째 서버 삭제 후 두 번째 서버 삭제
//        return firstServerClient.delete()
//                .uri("/users/" + id)
//                .retrieve()
//                .bodyToMono(Void.class)
//                .then(secondServerClient.delete()
//                        .uri("/users/" + id)
//                        .retrieve()
//                        .bodyToMono(Void.class));
//    }
//
//    public Mono<User> processUserUpdate(String id, User user) {
//        // 업데이트 로직은 생성 로직과 유사하게 순차적 호출과 롤백 구현
//        return firstServerClient.put()
//                .uri("/users/" + id)
//                .bodyValue(user)
//                .retrieve()
//                .bodyToMono(User.class)
//                .flatMap(firstServerResult -> secondServerClient.put()
//                        .uri("/users/" + id)
//                        .bodyValue(firstServerResult)
//                        .retrieve()
//                        .bodyToMono(User.class)
//                        .onErrorResume(e -> {
//                            System.err.println("Second server update failed, reverting first server...");
//                            return rollbackFirstServer(firstServerResult.getId()) // 롤백 로직은 비즈니스 요구사항에 맞게 구현
//                                    .then(Mono.error(e));
//                        })
//                        .thenReturn(firstServerResult));
//    }
//
//    // 첫 번째 서버에 롤백 명령 (삭제 요청)
//    private Mono<Void> rollbackFirstServer(String userId) {
//        return firstServerClient.delete()
//                .uri("/users/" + userId)
//                .retrieve()
//                .onStatus(HttpStatusCode::isError, response -> Mono.error(new RuntimeException("Rollback failed!")))
//                .bodyToMono(Void.class)
//                .onErrorResume(WebClientResponseException.class, e -> {
//                    System.err.println("Failed to rollback, user might be in an inconsistent state: " + e.getResponseBodyAsString());
//                    return Mono.empty();
//                });
//    }
}
