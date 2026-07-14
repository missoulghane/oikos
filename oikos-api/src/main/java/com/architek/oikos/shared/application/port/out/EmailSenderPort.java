package com.architek.oikos.shared.application.port.out;

import com.architek.oikos.shared.domain.valueobject.EmailVO;

/**
 * Generic outbound port for sending transactional emails. Deliberately free of
 * any business concept: subject/body composition is each feature's responsibility.
 */
public interface EmailSenderPort {

    void send(EmailVO to, String subject, String htmlBody);
}
