package com.architek.oikos.shared.application.port.out;

import com.architek.oikos.shared.domain.valueobject.PhoneNumberVO;

/**
 * Generic outbound port for sending WhatsApp messages, the exact counterpart of
 * EmailSenderPort: no business concept here either - what a message says is each
 * feature's responsibility, this port only knows how to hand it over.
 *
 * <p>Reminder on WhatsApp's own rule, which no adapter can work around: outside
 * the 24-hour window opened by a message FROM the recipient, only an approved
 * template goes through. Free-form text is for replies inside that window.
 */
public interface WhatsAppSenderPort {

    /** Limite WhatsApp pour un message texte, Unicode compris. */
    int MAX_TEXT_LENGTH = 4096;

    void sendText(PhoneNumberVO to, String text);

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
}
