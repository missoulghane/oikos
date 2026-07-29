package com.architek.oikos.shared.infrastructure.email;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.architek.oikos.shared.application.port.out.EmailSenderPort;
import com.architek.oikos.shared.domain.valueobject.EmailVO;
import com.architek.oikos.shared.exception.EmailDeliveryException;

/**
 * SMTP adapter (Gmail-compatible) implementing the generic EmailSenderPort. Holds
 * no business/domain concept: subject and body are supplied by the calling feature.
 * Disabled via oikos.mail.enabled=false, in favor of {@link LoggingEmailAdapter}.
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "oikos.mail", name = "enabled", havingValue = "true", matchIfMissing = true)
public class GmailEmailAdapter implements EmailSenderPort {

    private final JavaMailSender mailSender;

    @Value("${oikos.mail.from}")
    private String fromAddress;

    @Override
    public void send(EmailVO to, String subject, String htmlBody) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setTo(to.value());
            helper.setFrom(fromAddress);
            helper.setSubject(subject);
            helper.setText(htmlBody, true);
            mailSender.send(message);
        } catch (MessagingException | MailException e) {
            log.error("Failed to send email to {}", to.value(), e);
            throw new EmailDeliveryException("Failed to send email", e);
        }
    }
}
