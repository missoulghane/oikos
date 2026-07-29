package com.architek.oikos.shared.infrastructure.email;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

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

    @Override
    public void send(EmailVO to, String subject, String htmlBody) {
        log.debug("Email sending disabled (oikos.mail.enabled=false) - would have sent:\nTo: {}\nSubject: {}\nBody:\n{}",
                to.value(), subject, htmlBody);
    }
}
