package com.architek.oikos.auth.infrastructure.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

/**
 * Central Spring Security wiring, shared by the whole application: stateless JWT
 * auth, CSRF disabled (no cookies/sessions), and the public/permitAll surface for
 * auth + user registration/verification endpoints. @EnableWebSecurity is declared
 * explicitly (rather than relying on Spring Boot's implicit auto-detection) so the
 * HttpSecurity bean is available even in narrow test slices that import this
 * configuration directly (e.g. @WebMvcTest).
 */
@Configuration(proxyBeanMethods = false)
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfiguration {

    private final DomainUserDetailsService userDetailsService;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final RestAuthenticationEntryPoint authenticationEntryPoint;
    private final RestAccessDeniedHandler accessDeniedHandler;

    public SecurityConfiguration(DomainUserDetailsService userDetailsService,
                                  PasswordEncoder passwordEncoder,
                                  JwtService jwtService,
                                  RestAuthenticationEntryPoint authenticationEntryPoint,
                                  RestAccessDeniedHandler accessDeniedHandler) {
        this.userDetailsService = userDetailsService;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.authenticationEntryPoint = authenticationEntryPoint;
        this.accessDeniedHandler = accessDeniedHandler;
    }

    @Bean
    public AuthenticationManager authenticationManager() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder);
        return new ProviderManager(provider);
    }

    /**
     * Defaults to localhost/127.0.0.1 (any port): covers the Vite dev server,
     * the Expo web/Metro dev server, and native app builds hitting a locally
     * forwarded port, without opening the API to arbitrary third-party origins.
     * Recette/prod override via oikos.security.cors.allowed-origins
     * (APP_CORS_ALLOWED_ORIGINS) with the real public origin(s) of oikos-web.
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource(
            @Value("#{'${oikos.security.cors.allowed-origins}'.split(',')}") List<String> allowedOrigins) {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(allowedOrigins);
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http,
                                                     CorsConfigurationSource corsConfigurationSource) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configurationSource(corsConfigurationSource))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(handling -> handling
                        .authenticationEntryPoint(authenticationEntryPoint)
                        .accessDeniedHandler(accessDeniedHandler))
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers("/api/v1/auth/**").permitAll()
                        .requestMatchers("/api/v1/users/register-property-user",
                                "/api/v1/users/register-property-board-admin",
                                "/api/v1/users/register-property-manager-admin",
                                "/api/v1/users/verify", "/api/v1/users/resend-verification",
                                "/api/v1/users/activate-account", "/api/v1/users/accept-invitation",
                                // Step 1 of the volunteer-syndic wizard, typed before any
                                // account can exist (see CaptureOnboardingLeadService).
                                "/api/v1/users/onboarding-leads").permitAll()
                        // GET (preview/available-units) stays anonymous so the invitation
                        // landing page renders before the visitor logs in; the POST endpoints
                        // (accept/membership-requests) fall through to .anyRequest().authenticated()
                        // below so a missing/expired token is rejected at this filter (401,
                        // triggers the frontend's silent-refresh-and-retry) instead of reaching
                        // the controller's @PreAuthorize check (403, no retry).
                        .requestMatchers(HttpMethod.GET, "/api/v1/invitations/by-token/**").permitAll()
                        .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()
                        .requestMatchers("/actuator/health", "/actuator/info", "/actuator/prometheus").permitAll()
                        .requestMatchers("/h2-console/**").permitAll()
                        .anyRequest().authenticated())
                .addFilterBefore(new JwtAuthenticationFilter(jwtService), UsernamePasswordAuthenticationFilter.class)
                .headers(headers -> headers.frameOptions(frameOptions -> frameOptions.sameOrigin()));

        return http.build();
    }
}
