package ai.duclo.scimtest.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@Slf4j
@RestController
@RequestMapping("/scim")
public class HealthCheckController {
    @GetMapping("/")
    @ResponseStatus(HttpStatus.OK)
    public Mono<ResponseEntity<Object>> geHealthCheckHome() {
        return Mono.just( ResponseEntity.ok().body(null));
    }

    @GetMapping("/health")
    @ResponseStatus(HttpStatus.OK)
    public Mono<ResponseEntity<Object>> geHealthCheck() {
        return Mono.just( ResponseEntity.ok().body(null));
    }
}
