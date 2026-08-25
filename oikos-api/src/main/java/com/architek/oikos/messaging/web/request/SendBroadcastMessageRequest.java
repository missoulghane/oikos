package com.architek.oikos.messaging.web.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Un envoi à toute la copropriété porte son objet, comme n'importe quel autre
 * message - il ne rejoint plus un canal d'annonces dont le titre était fixé par
 * l'interface (voir SendBroadcastMessageService). Pas de senderIdentity : un
 * envoi groupé part toujours au nom du bureau.
 */
public record SendBroadcastMessageRequest(@NotBlank @Size(max = 200) String subject,
                                            @NotBlank @Size(max = 4000) String body) {
}
