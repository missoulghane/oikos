package com.architek.oikos.shared.infrastructure.email;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import com.architek.oikos.shared.application.port.out.AsyncEmailSenderPort;
import com.architek.oikos.shared.application.port.out.EmailSenderPort;
import com.architek.oikos.shared.domain.valueobject.EmailVO;

/**
 * Fire-and-forget wrapper around EmailSenderPort for call sites that must not
 * make the caller wait on the SMTP round-trip - e.g. the verification email
 * sent at the end of registration, whose latency was otherwise felt as the
 * request hanging. Failures are still logged by SmtpEmailAdapter itself; there
 * is simply no one left synchronously waiting to react to them.
 */
@Component
public class AsyncEmailSender implements AsyncEmailSenderPort {

    private final EmailSenderPort emailSenderPort;

    public AsyncEmailSender(EmailSenderPort emailSenderPort) {
        this.emailSenderPort = emailSenderPort;
    }

    @Override
    @Async
    public void send(EmailVO to, String subject, String htmlBody) {
        emailSenderPort.send(to, subject, htmlBody);
    }
}
