package com.architek.oikos.shared.infrastructure.ratelimit;

import java.util.Locale;

import org.springframework.stereotype.Component;

import com.architek.oikos.shared.application.port.out.EmailSendQuotaPort;
import com.architek.oikos.shared.domain.service.RateLimiter;
import com.architek.oikos.shared.exception.TooManyRequestsException;

@Component
public class RateLimitedEmailSendQuota implements EmailSendQuotaPort {

    private final RateLimiter rateLimiter;
    private final RateLimitPolicies policies;

    public RateLimitedEmailSendQuota(RateLimiter rateLimiter, RateLimitPolicies policies) {
        this.rateLimiter = rateLimiter;
        this.policies = policies;
    }

    @Override
    public void requireQuota(String emailAddress) {
        if (!policies.enabled()) {
            return;
        }
        // Minuscules : sans cela « Jane@Doe.com » et « jane@doe.com » ouvriraient
        // deux compteurs pour une seule boîte, et le plafond se multiplierait par
        // le nombre de casses qu'un appelant a la patience d'essayer.
        String key = "email|" + emailAddress.toLowerCase(Locale.ROOT);
        RateLimiter.Decision decision = rateLimiter.tryConsume(key, policies.emailSendPerAddress(), policies.window());
        if (decision.rejected()) {
            throw new TooManyRequestsException(
                    "Un email vient déjà d'être envoyé à cette adresse. Merci de patienter quelques minutes.",
                    decision.retryAfter());
        }
    }
}
