package com.architek.oikos.shared.infrastructure.whatsapp;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import com.architek.oikos.shared.application.port.out.WhatsAppReplyButton;
import com.architek.oikos.shared.application.port.out.WhatsAppSenderPort;
import com.architek.oikos.shared.domain.valueobject.PhoneNumberVO;
import com.architek.oikos.shared.exception.WhatsAppDeliveryException;

/**
 * Vonage Messages API adapter (POST {api-region}.nexmo.com/v1/messages, channel
 * "whatsapp") implementing the generic WhatsAppSenderPort. Holds no
 * business/domain concept: the text and the button labels are supplied by the
 * calling feature.
 *
 * <p>Authenticates with HTTP Basic (API key/secret) rather than the JWT scheme:
 * both are accepted by the Messages API, and JWT would add a signing key to
 * deploy and rotate for the sole benefit of ACLs, which a single-sender
 * integration has no use for.
 *
 * <p>Enabled by oikos.whatsapp.enabled=true, in favor of
 * {@link LoggingWhatsAppAdapter} - the reverse default of the mail adapters, and
 * for the same reason as push: no WhatsApp Business account is attached yet, so
 * the real adapter must be opted into rather than opted out of.
 */
@Slf4j
@Component
@ConditionalOnProperty(prefix = "oikos.whatsapp", name = "enabled", havingValue = "true")
public class VonageWhatsAppAdapter implements WhatsAppSenderPort {

    private static final String CHANNEL = "whatsapp";

    private final RestClient restClient;
    private final String messagesUrl;
    private final String apiKey;
    private final String apiSecret;
    private final String fromNumber;

    /**
     * Le client HTTP est construit ici plutôt qu'injecté : Spring Boot 4 n'expose
     * plus de bean RestClient.Builder par défaut avec le starter webmvc, et
     * ExpoPushAdapter fait déjà de même. Le constructeur ci-dessous reste ouvert
     * aux tests, qui ont besoin d'un builder pour y brancher MockRestServiceServer.
     */
    @Autowired
    public VonageWhatsAppAdapter(@Value("${oikos.whatsapp.messages-url}") String messagesUrl,
                                  @Value("${oikos.whatsapp.api-key}") String apiKey,
                                  @Value("${oikos.whatsapp.api-secret}") String apiSecret,
                                  @Value("${oikos.whatsapp.from}") String fromNumber) {
        this(RestClient.builder(), messagesUrl, apiKey, apiSecret, fromNumber);
    }

    VonageWhatsAppAdapter(RestClient.Builder restClientBuilder, String messagesUrl, String apiKey, String apiSecret,
                           String fromNumber) {
        this.restClient = restClientBuilder.build();
        this.messagesUrl = messagesUrl;
        this.apiKey = apiKey;
        this.apiSecret = apiSecret;
        this.fromNumber = fromNumber;
    }

    /**
     * Startup banner, same role as SmtpEmailAdapter's: say which adapter won the
     * &#64;ConditionalOnProperty race and against which endpoint. The failure is
     * raised here rather than at the first send - an instance started with
     * whatsapp.enabled=true and no credentials would otherwise look healthy until
     * the first message silently 401s. Neither the secret nor the API key is logged.
     */
    @PostConstruct
    void logConfiguration() {
        if (apiKey.isBlank() || apiSecret.isBlank() || fromNumber.isBlank()) {
            throw new IllegalStateException(
                    "oikos.whatsapp.enabled=true requires oikos.whatsapp.api-key, .api-secret and .from to be set");
        }
        log.info("WhatsApp sending ENABLED (VonageWhatsAppAdapter) - endpoint {}, From: {}", messagesUrl, fromNumber);
    }

    @Override
    public void sendText(PhoneNumberVO to, String text) {
        WhatsAppSenderPort.checkText(text);
        Map<String, Object> payload = message(to, "text");
        payload.put("text", text);
        post(to, payload, "text");
    }

    @Override
    public void sendReplyButtons(PhoneNumberVO to, String header, String body, String footer,
                                  List<WhatsAppReplyButton> buttons) {
        WhatsAppSenderPort.checkReplyButtons(body, buttons);

        // Message interactif = message_type "custom" : l'objet "custom" est repris
        // tel quel par WhatsApp, Vonage ne fait que le transporter (voir « Working
        // with WhatsApp Interactive Messages »).
        Map<String, Object> interactive = new LinkedHashMap<>();
        interactive.put("type", "button");
        if (header != null && !header.isBlank()) {
            interactive.put("header", Map.of("type", "text", "text", header));
        }
        interactive.put("body", Map.of("text", body));
        if (footer != null && !footer.isBlank()) {
            interactive.put("footer", Map.of("text", footer));
        }
        List<Map<String, Object>> replies = new ArrayList<>();
        for (WhatsAppReplyButton button : buttons) {
            replies.add(Map.of("type", "reply", "reply", Map.of("id", button.id(), "title", button.title())));
        }
        interactive.put("action", Map.of("buttons", replies));

        Map<String, Object> payload = message(to, "custom");
        payload.put("custom", Map.of("type", "interactive", "interactive", interactive));
        post(to, payload, "interactive");
    }

    /**
     * Le corps d'erreur Vonage porte le vrai diagnostic (1120 « expéditeur
     * invalide », 1010 « hors fenêtre de 24 h », 1140 « numéro non inscrit au bac
     * à sable »...). Sans lui, un refus remonte comme un 4xx anonyme et il faut
     * ouvrir les logs du serveur pour savoir quoi corriger - alors que ce qui est
     * à corriger est justement une configuration.
     */
    private static String refusalReason(RestClientResponseException e) {
        try {
            Map<?, ?> body = e.getResponseBodyAs(Map.class);
            if (body != null && body.get("title") != null) {
                return "%s - %s (%s)".formatted(body.get("title"), body.get("detail"), body.get("type"));
            }
        } catch (RestClientException ignored) {
            // Corps illisible : le statut HTTP reste plus parlant que rien.
        }
        return "HTTP " + e.getStatusCode().value();
    }

    private Map<String, Object> message(PhoneNumberVO to, String messageType) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("from", fromNumber);
        payload.put("to", to.value());
        payload.put("channel", CHANNEL);
        payload.put("message_type", messageType);
        return payload;
    }

    private void post(PhoneNumberVO to, Map<String, Object> payload, String kind) {
        try {
            Map<?, ?> response = restClient.post()
                    .uri(messagesUrl)
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON)
                    .headers(headers -> headers.setBasicAuth(apiKey, apiSecret))
                    .body(payload)
                    .retrieve()
                    .body(Map.class);
            // Le destinataire et l'identifiant renvoyé, jamais le contenu : un message
            // peut porter un lien ou un code, qui n'a rien à faire dans un log par défaut.
            log.info("WhatsApp {} message accepted by Vonage - to: {}, message_uuid: {}",
                    kind, to.value(), response == null ? "unknown" : response.get("message_uuid"));
        } catch (RestClientResponseException e) {
            log.error("Vonage rejected the WhatsApp {} message to {} - HTTP {}: {}",
                    kind, to.value(), e.getStatusCode().value(), e.getResponseBodyAsString(), e);
            throw new WhatsAppDeliveryException("Vonage a refusé le message WhatsApp : " + refusalReason(e), e);
        } catch (RestClientException e) {
            log.error("Failed to reach the Vonage Messages API for the WhatsApp {} message to {}", kind, to.value(), e);
            throw new WhatsAppDeliveryException(
                    "L'API Vonage est injoignable (" + messagesUrl + ") : " + e.getMessage(), e);
        }
    }
}
