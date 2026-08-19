package com.architek.oikos.integration;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import com.architek.oikos.shared.application.port.out.WhatsAppSenderPort;
import com.architek.oikos.shared.infrastructure.whatsapp.LoggingWhatsAppAdapter;
import com.architek.oikos.shared.infrastructure.whatsapp.VonageWhatsAppAdapter;

/**
 * Le contexte démarre-t-il vraiment avec l'envoi WhatsApp activé ? Les tests
 * unitaires de l'adaptateur, eux, le construisent à la main : ils n'auraient pas
 * vu que RestClient.Builder n'est plus un bean auto-configuré depuis Spring Boot
 * 4, ce qui empêchait l'application de démarrer dès que oikos.whatsapp.enabled
 * passait à true.
 */
class WhatsAppAdapterWiringTest {

    @Nested
    @SpringBootTest
    @TestPropertySource(properties = {
            "oikos.whatsapp.enabled=true",
            "oikos.whatsapp.api-key=test-key",
            "oikos.whatsapp.api-secret=test-secret",
            "oikos.whatsapp.from=212600000000",
    })
    class WhenSendingIsEnabled {

        @Autowired
        private WhatsAppSenderPort whatsAppSenderPort;

        @Test
        void the_real_adapter_is_wired() {
            assertThat(whatsAppSenderPort).isInstanceOf(VonageWhatsAppAdapter.class);
        }
    }

    @Nested
    @SpringBootTest
    class WhenSendingIsDisabledByDefault {

        @Autowired
        private WhatsAppSenderPort whatsAppSenderPort;

        @Test
        void the_logging_adapter_takes_over() {
            assertThat(whatsAppSenderPort).isInstanceOf(LoggingWhatsAppAdapter.class);
        }
    }
}
