package ai.duclo.scimtest.filter;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import javax.crypto.SecretKey;

@Slf4j
//@Component
public class JwtAuthenticationFilter implements WebFilter {

    //TODO 임시 키
    private final String SAMPLE_SECRET_KEY = "5eR$9kL#7tF@2zQwP8dS&6uV!3mH*1xYb";

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        String header = exchange.getRequest().getHeaders().getFirst("Authorization");
        SecretKey key = Keys.hmacShaKeyFor(SAMPLE_SECRET_KEY.getBytes());

        if (StringUtils.hasText(header) && header.startsWith("Bearer ")) {
            String token = header.substring(7);
            try {
                Claims claims = Jwts.parser()
                        .verifyWith(key)
                        .build()
                        .parseSignedClaims(token)
                        .getPayload();

                String username = claims.getSubject();
                log.info("username - {}", username);
                if (username != null) {
                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(username, null, null);

                    // SecurityContext에 인증 정보 저장
                    return chain.filter(exchange)
                            .contextWrite(ReactiveSecurityContextHolder.withSecurityContext(
                                    Mono.just(new SecurityContextImpl(authentication)))
                            );
                }
            } catch (ExpiredJwtException ex) {
                log.info("Token expired: {}", ex.getMessage());
            } catch (UnsupportedJwtException ex) {
                log.info("Unsupported JWT: {}", ex.getMessage());
            } catch (MalformedJwtException ex) {
                log.info("Malformed JWT: {}", ex.getMessage());
            } catch (IllegalArgumentException ex) {
                log.info("Illegal argument: {}", ex.getMessage());
            } catch (JwtException ex) {
                log.info("JWT exception: {}", ex.getMessage());
            } catch (Exception e) {
                log.info("Unknown jwt error: {}", e.getMessage());
            }
        }

        // 인증 실패(혹은 jwt 없음)은 그냥 다음 필터 진행 (401 처리 원하면 아래 주석 참고)
        return chain.filter(exchange);

        // 인증 실패시 401 반환하려면 아래 코드로 교체 가능
        /*
        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
        return exchange.getResponse().setComplete();
        */
    }

}
