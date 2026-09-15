package com.architek.oikos.shared.application.port.out;

import com.architek.oikos.shared.domain.valueobject.EmailVO;

/**
 * Fire-and-forget counterpart to EmailSenderPort: for call sites that must not
 * make the caller wait on the SMTP round-trip - e.g. the verification email
 * sent at the end of a registration request.
 */
public interface AsyncEmailSenderPort {

    void send(EmailVO to, String subject, String htmlBody);
}
