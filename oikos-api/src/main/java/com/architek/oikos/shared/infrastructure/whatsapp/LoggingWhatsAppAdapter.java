package com.architek.oikos.shared.infrastructure.whatsapp;

import java.util.List;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import com.architek.oikos.shared.application.port.out.WhatsAppReplyButton;
import com.architek.oikos.shared.application.port.out.WhatsAppSenderPort;
import com.architek.oikos.shared.domain.valueobject.PhoneNumberVO;

/**
 * No-op adapter used when oikos.whatsapp.enabled=false (the default - see
 * VonageWhatsAppAdapter's javadoc): prints what would have been sent instead of
 * calling Vonage, so WhatsApp-triggering flows stay testable without a WhatsApp
 * Business account. Runs the same validation as the real adapter, so a message
 * that would be refused in production is refused here too.
 */
@Slf4j
@Component
@ConditionalOnProperty(prefix = "oikos.whatsapp", name = "enabled", havingValue = "false", matchIfMissing = true)
public class LoggingWhatsAppAdapter implements WhatsAppSenderPort {

    @PostConstruct
    void logConfiguration() {
        log.info("WhatsApp sending DISABLED (oikos.whatsapp.enabled=false) - no message will leave this instance");
    }

    @Override
    public void sendText(PhoneNumberVO to, String text) {
        WhatsAppSenderPort.checkText(text);
        log.info("WhatsApp sending disabled - would have sent a text message to: {}", to.value());
        log.debug("Message that would have been sent:\n{}", text);
    }

    @Override
    public void sendReplyButtons(PhoneNumberVO to, String header, String body, String footer,
                                  List<WhatsAppReplyButton> buttons) {
        WhatsAppSenderPort.checkReplyButtons(body, buttons);
        log.info("WhatsApp sending disabled - would have sent an interactive message to: {} with {} button(s)",
                to.value(), buttons.size());
        log.debug("Message that would have been sent:\nHeader: {}\nBody: {}\nFooter: {}\nButtons: {}",
                header, body, footer, buttons);
    }
}
