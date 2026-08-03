package com.br.pokefichas.commons.config;

import com.br.pokefichas.commons.security.JwtAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(securedEnabled = true, prePostEnabled = true)
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final CorsProperties corsProperties;

    public SecurityConfig(final JwtAuthenticationFilter jwtAuthenticationFilter,
                          final CorsProperties corsProperties) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.corsProperties = corsProperties;
    }

    @Bean
    @Order(0)
    public SecurityFilterChain publicSecurityFilterChain(final HttpSecurity http) throws Exception {
        http
                .securityMatcher(
                        "/auth/login",
                        "/auth/registrar",
                        "/auth/refresh",
                        "/auth/csrf",
                        "/bootstrap",
                        "/fichas/publicas/**",
                        "/public/**",
                        "/pokemon/custom",
                        "/pokemon/custom/**",
                        "/catalogo/pokemon/status",
                        "/actuator/**",
                        "/swagger-ui/**",
                        "/swagger-ui.html",
                        "/api-docs/**",
                        "/v3/api-docs/**",
                        "/api/auth/login",
                        "/api/auth/registrar",
                        "/api/auth/refresh",
                        "/api/auth/csrf",
                        "/api/bootstrap",
                        "/api/fichas/publicas/**",
                        "/api/public/**",
                        "/api/pokemon/custom",
                        "/api/pokemon/custom/**",
                        "/api/catalogo/pokemon/status",
                        "/api/actuator/**",
                        "/api/swagger-ui/**",
                        "/api/swagger-ui.html",
                        "/api/api-docs/**",
                        "/api/v3/api-docs/**"
                )
                .csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .authorizeHttpRequests(authz -> authz.anyRequest().permitAll())
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                );

        return http.build();
    }

    @Bean
    @Order(1)
    public SecurityFilterChain securityFilterChain(final HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .authorizeHttpRequests(authz -> authz
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()


                        .requestMatchers("/auth/login", "/auth/registrar", "/auth/refresh", "/auth/csrf", "/bootstrap").permitAll()
                        .requestMatchers("/public/**", "/api/public/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/fichas/publicas/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/pokemon/custom", "/pokemon/custom/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/catalogo/pokemon/status").permitAll()
                        .requestMatchers("/actuator/**").permitAll()
                        .requestMatchers(
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/api-docs/**",
                                "/v3/api-docs/**"
                        ).permitAll()
                        .anyRequest().authenticated()
                )
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public WebSecurityCustomizer webSecurityCustomizer() {
        return web -> web.ignoring().requestMatchers(
                "/auth/login",
                "/auth/registrar",
                "/auth/refresh",
                "/auth/csrf",
                "/bootstrap",
                "/fichas/publicas/**",
                "/public/**",
                "/pokemon/custom",
                "/pokemon/custom/**",
                "/catalogo/pokemon/status",
                "/actuator/**",
                "/swagger-ui/**",
                "/swagger-ui.html",
                "/api-docs/**",
                "/v3/api-docs/**",
                "/api/auth/login",
                "/api/auth/registrar",
                "/api/auth/refresh",
                "/api/auth/csrf",
                "/api/bootstrap",
                "/api/fichas/publicas/**",
                "/api/public/**",
                "/api/pokemon/custom",
                "/api/pokemon/custom/**",
                "/api/catalogo/pokemon/status",
                "/api/actuator/**",
                "/api/swagger-ui/**",
                "/api/swagger-ui.html",
                "/api/api-docs/**",
                "/api/v3/api-docs/**"
        );
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        final CorsConfiguration configuration = new CorsConfiguration();

        final List<String> allowedOriginPatterns = corsProperties.getAllowedOriginPatterns().isEmpty()
                ? List.of("https://*.vercel.app", "http://localhost:*", "http://127.0.0.1:*")
                : corsProperties.getAllowedOriginPatterns();
        configuration.setAllowedOriginPatterns(allowedOriginPatterns);

        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));

        configuration.setAllowedHeaders(List.of(
                "Authorization",
                "Content-Type",
                "Accept",
                "X-Requested-With"
        ));

        configuration.setExposedHeaders(List.of("Authorization", "X-Total-Count"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);

        final UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
