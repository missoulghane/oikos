package com.architek.oikos.shared.infrastructure.whatsapp;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.jsonPath;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withBadRequest;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import java.util.Base64;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import com.architek.oikos.shared.domain.valueobject.PhoneNumberVO;
import com.architek.oikos.shared.exception.WhatsAppDeliveryException;

/**
 * Le contrat vérifié ici est celui de l'API Messages de Vonage, pas le nôtre :
 * la forme exacte du corps envoyé (« Working with WhatsApp Interactive
 * Messages »). Un champ renommé ne se voit pas au compilateur, seulement à un
 * refus en production.
 */
class VonageWhatsAppAdapterTest {

    private static final String MESSAGES_URL = "https://api.nexmo.com/v1/messages";
    private static final String ACCEPTED = "{\"message_uuid\":\"aaaaaaaa-bbbb-4ccc-8ddd-0123456789ab\"}";

    private final PhoneNumberVO recipient = PhoneNumberVO.of("+212612345678");

    private MockRestServiceServer server;
    private VonageWhatsAppAdapter adapter;

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder();
        server = MockRestServiceServer.bindTo(builder).build();
        adapter = new VonageWhatsAppAdapter(builder, MESSAGES_URL, "api-key", "api-secret", "212600000000");
    }

    @Test
    void sends_a_text_message_on_the_whatsapp_channel() {
        String expectedBasic = "Basic " + Base64.getEncoder().encodeToString("api-key:api-secret".getBytes());
        server.expect(requestTo(MESSAGES_URL))
                .andExpect(method(HttpMethod.POST))
                .andExpect(header("Authorization", expectedBasic))
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.channel").value("whatsapp"))
                .andExpect(jsonPath("$.message_type").value("text"))
                .andExpect(jsonPath("$.from").value("212600000000"))
                // Le '+' du numéro saisi doit avoir disparu : Vonage refuse le format.
                .andExpect(jsonPath("$.to").value("212612345678"))
                .andExpect(jsonPath("$.text").value("Votre convocation est disponible."))
                .andRespond(withSuccess(ACCEPTED, MediaType.APPLICATION_JSON));

        adapter.sendText(recipient, "Votre convocation est disponible.");

        server.verify();
    }

    @Test
    void carries_the_vonage_refusal_reason_into_the_delivery_exception() {
        // Le motif exact est ce qui distingue « à corriger dans la configuration »
        // de « à réessayer » : le perdre imposait d'aller lire les logs serveur.
        server.expect(requestTo(MESSAGES_URL))
                .andRespond(withBadRequest().contentType(MediaType.APPLICATION_JSON)
                        .body("{\"title\":\"Invalid sender\",\"detail\":\"The `from` parameter is invalid.\","
                                + "\"type\":\"https://developer.vonage.com/api-errors/messages#1120\"}"));

        assertThatThrownBy(() -> adapter.sendText(recipient, "Bonjour"))
                .isInstanceOf(WhatsAppDeliveryException.class)
                .hasMessageContaining("Invalid sender")
                .hasMessageContaining("The `from` parameter is invalid.")
                .hasMessageContaining("messages#1120");
    }

    @Test
    void falls_back_to_the_http_status_when_the_error_body_is_unreadable() {
        server.expect(requestTo(MESSAGES_URL))
                .andRespond(withBadRequest().contentType(MediaType.TEXT_HTML).body("<html>502</html>"));

        assertThatThrownBy(() -> adapter.sendText(recipient, "Bonjour"))
                .isInstanceOf(WhatsAppDeliveryException.class)
                .hasMessageContaining("HTTP 400");
    }

    @Test
    void refuses_a_text_longer_than_whatsapp_accepts() {
        assertThatThrownBy(() -> adapter.sendText(recipient, "a".repeat(4097)))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
