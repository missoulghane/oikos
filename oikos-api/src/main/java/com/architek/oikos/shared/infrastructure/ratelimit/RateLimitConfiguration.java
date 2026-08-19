package com.architek.oikos.shared.infrastructure.ratelimit;

import java.time.Clock;
import java.time.Duration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.security.autoconfigure.web.servlet.SecurityFilterProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.architek.oikos.shared.domain.service.RateLimiter;

import tools.jackson.databind.ObjectMapper;

/**
 * Câblage de l'anti-abus.
 *
 * <p>Le filtre est enregistré comme filtre de servlet ordinaire, juste avant la
 * chaîne Spring Security (dont l'ordre est {@code SecurityFilterProperties
 * .DEFAULT_FILTER_ORDER}, -100), et non
 * ajouté dans cette chaîne : une tentative de mot de passe doit être comptée
 * avant d'être authentifiée, et la chaîne de sécurité est chargée par toutes les
 * tranches @WebMvcTest, qu'un plafond partagé entre leurs cas de test rendrait
 * dépendantes de leur ordre d'exécution.
 */
@Configuration(proxyBeanMethods = false)
public class RateLimitConfiguration {

    @Bean
    public RateLimitPolicies rateLimitPolicies(
            @Value("${oikos.security.rate-limit.enabled}") boolean enabled,
            @Value("${oikos.security.rate-limit.window}") Duration window,
            @Value("${oikos.security.rate-limit.login-per-ip}") int loginPerIp,
            @Value("${oikos.security.rate-limit.email-send-per-ip}") int emailSendPerIp,
            @Value("${oikos.security.rate-limit.token-attempt-per-ip}") int tokenAttemptPerIp,
            @Value("${oikos.security.rate-limit.registration-per-ip}") int registrationPerIp,
            @Value("${oikos.security.rate-limit.email-send-per-address}") int emailSendPerAddress) {
        return new RateLimitPolicies(enabled, window, loginPerIp, emailSendPerIp, tokenAttemptPerIp,
                registrationPerIp, emailSendPerAddress);
    }

    @Bean
    public RateLimiter rateLimiter(Clock clock) {
        return new RateLimiter(clock);
    }

    @Bean
    @ConditionalOnProperty(prefix = "oikos.security.rate-limit", name = "enabled", havingValue = "true")
    public FilterRegistrationBean<RateLimitFilter> rateLimitFilterRegistration(RateLimiter rateLimiter,
                                                                               RateLimitPolicies policies,
                                                                               ObjectMapper objectMapper) {
        FilterRegistrationBean<RateLimitFilter> registration =
                new FilterRegistrationBean<>(new RateLimitFilter(rateLimiter, policies, objectMapper));
        registration.setOrder(SecurityFilterProperties.DEFAULT_FILTER_ORDER - 5);
        return registration;
    }
}
