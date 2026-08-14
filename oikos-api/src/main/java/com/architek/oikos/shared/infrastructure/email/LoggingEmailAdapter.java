package com.architek.oikos.shared.infrastructure.email;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import com.architek.oikos.shared.application.port.out.EmailSenderPort;
import com.architek.oikos.shared.domain.valueobject.EmailVO;

/**
 * No-op adapter used when oikos.mail.enabled=false: instead of sending anything,
 * it prints the email content to the application log at DEBUG level, so mail-triggering
 * flows (registration, invitations, password reset...) stay testable without SMTP.
 */
@Slf4j
@Component
@ConditionalOnProperty(prefix = "oikos.mail", name = "enabled", havingValue = "false")
public class LoggingEmailAdapter implements EmailSenderPort {

    /**
     * WARN, not DEBUG: this adapter is silent by nature, and a deployment that
     * ships with it by accident produces no mail and no error at all. The one
     * line at startup is what makes that visible.
     */
    @PostConstruct
    void logConfiguration() {
        log.warn("Email sending DISABLED (oikos.mail.enabled=false) - no mail will leave this instance");
    }

    @Override
    public void send(EmailVO to, String subject, String htmlBody) {
        // INFO for the fact, DEBUG for the body: the body carries the
        // verification/reset token, which has no place in a default-level log.
        log.info("Email sending disabled - would have sent to: {}, subject: {}", to.value(), subject);
        log.debug("Body that would have been sent:\n{}", htmlBody);
    }
}
