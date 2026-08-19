package com.architek.oikos.shared.application.port.out;

import java.util.List;

import com.architek.oikos.shared.domain.valueobject.PhoneNumberVO;

/**
 * Generic outbound port for sending WhatsApp messages, the exact counterpart of
 * EmailSenderPort: no business concept here either - what a message says is each
 * feature's responsibility, this port only knows how to hand it over.
 *
 * <p>Two shapes, because WhatsApp itself has two: a plain text message, and an
 * interactive one whose reply buttons come back as an inbound webhook event
 * (see the Vonage Messages API, WhatsApp interactive messages).
 *
 * <p>Reminder on WhatsApp's own rule, which no adapter can work around: outside
 * the 24-hour window opened by a message FROM the recipient, only an approved
 * template goes through. Free-form text is for replies inside that window.
 */
public interface WhatsAppSenderPort {

    /** Limite WhatsApp pour un message texte, Unicode compris. */
    int MAX_TEXT_LENGTH = 4096;

    /** Limite WhatsApp pour un message à boutons de réponse. */
    int MAX_REPLY_BUTTONS = 3;

    /** Limite WhatsApp pour le corps d'un message interactif. */
    int MAX_BODY_LENGTH = 1024;

    void sendText(PhoneNumberVO to, String text);

    /**
     * Interactive message: a body and up to {@value #MAX_REPLY_BUTTONS} buttons.
     * {@code header} and {@code footer} are optional (null to omit them).
     */
    void sendReplyButtons(PhoneNumberVO to, String header, String body, String footer, List<WhatsAppReplyButton> buttons);

    /**
     * Contrôles partagés par tous les adaptateurs, appelés avant l'envoi : les
     * poser ici plutôt que dans l'adaptateur Vonage garantit qu'un message
     * refusé en production l'est aussi en développement, où l'adaptateur de log
     * l'aurait sinon accepté sans broncher.
     */
    static void checkText(String text) {
        if (text == null || text.isBlank()) {
            throw new IllegalArgumentException("WhatsApp text must not be blank");
        }
        if (text.length() > MAX_TEXT_LENGTH) {
            throw new IllegalArgumentException(
                    "WhatsApp text exceeds " + MAX_TEXT_LENGTH + " characters: " + text.length());
        }
    }

    static void checkReplyButtons(String body, List<WhatsAppReplyButton> buttons) {
        if (body == null || body.isBlank()) {
            throw new IllegalArgumentException("WhatsApp interactive body must not be blank");
        }
        if (body.length() > MAX_BODY_LENGTH) {
            throw new IllegalArgumentException(
                    "WhatsApp interactive body exceeds " + MAX_BODY_LENGTH + " characters: " + body.length());
        }
        if (buttons == null || buttons.isEmpty() || buttons.size() > MAX_REPLY_BUTTONS) {
            throw new IllegalArgumentException(
                    "A WhatsApp interactive message carries 1 to " + MAX_REPLY_BUTTONS + " reply buttons");
        }
        if (buttons.stream().map(WhatsAppReplyButton::id).distinct().count() != buttons.size()) {
            // Deux boutons de même id : le webhook entrant ne permettrait plus de
            // savoir lequel a été touché.
            throw new IllegalArgumentException("WhatsApp reply button ids must be unique");
        }
    }
}
