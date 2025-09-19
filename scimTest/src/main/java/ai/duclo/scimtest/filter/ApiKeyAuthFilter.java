package ai.duclo.scimtest.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;


@Slf4j
//@Component
public class ApiKeyAuthFilter implements WebFilter {
    private static final String API_KEY_HEADER = "X-API-KEY";
    private final String SAMPLE_SECRET_KEY = "duclo-secret-api-key";    //임시 키

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String apikey = request.getHeaders().getFirst(API_KEY_HEADER);

        if (StringUtils.hasLength(apikey) && SAMPLE_SECRET_KEY.equals(apikey)) {
            log.info("API KEY Authenticated");
            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(apikey, null, null);

            // SecurityContext에 인증 정보 저장
            return chain.filter(exchange)
                    .contextWrite(ReactiveSecurityContextHolder.withSecurityContext(
                            Mono.just(new SecurityContextImpl(authentication)))
                    );
        } else {
            log.info("No security context");
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }
    }

}