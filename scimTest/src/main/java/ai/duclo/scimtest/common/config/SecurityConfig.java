package ai.duclo.scimtest.common.config;

import ai.duclo.scimtest.common.filter.ApiKeyAuthFilter;
import ai.duclo.scimtest.common.filter.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.context.NoOpServerSecurityContextRepository;
import org.springframework.security.web.server.util.matcher.ServerWebExchangeMatchers;

@Configuration
@EnableWebFluxSecurity
@RequiredArgsConstructor
public class SecurityConfig {

//    private final JwtAuthenticationFilter jwtAuthenticationFilter;
//    private final ApiKeyAuthFilter apiKeyAuthFilter;

    @Bean
    @Order(1)
    public SecurityWebFilterChain apiKeyFilterChain(ServerHttpSecurity http) {
        return http
                .securityMatcher(ServerWebExchangeMatchers.pathMatchers("/scim/internal/v1/**"))
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .cors(cors -> {})
                .securityContextRepository(NoOpServerSecurityContextRepository.getInstance())
//                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
//                .addFilterAt(apiKeyAuthFilter, SecurityWebFiltersOrder.AUTHENTICATION)
//                .addFilterAt(new ApiKeyAuthFilter(), SecurityWebFiltersOrder.AUTHENTICATION)
                .authorizeExchange(authz -> authz
//                        .pathMatchers("/api/v1/internal/**").authenticated()
                        .anyExchange().permitAll()
                )
                .build();
    }

    @Bean
    @Order(2)
    SecurityWebFilterChain jwtApiSecurityFilterChain(ServerHttpSecurity http) {
        return http
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .cors(cors -> {})
                .securityContextRepository(NoOpServerSecurityContextRepository.getInstance()) // STATELESS
//                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeExchange(authz -> authz
                        .pathMatchers(
                                "/scim/",
                                "/scim/health",
                                "/swagger-ui/**",
                                "/v3/api-docs/**",
                                "/swagger-ui.html",
                                "/webjars/**",
                                "/swagger-resources/**"
                        ).permitAll()
                        .anyExchange().authenticated()
                )
//                .addFilterAt(jwtAuthenticationFilter, SecurityWebFiltersOrder.AUTHENTICATION)
                .addFilterAt(new JwtAuthenticationFilter(), SecurityWebFiltersOrder.AUTHENTICATION)
                .build();
    }
}
