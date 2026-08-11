package com.architek.oikos.messaging.domain.model;

/**
 * Which hat a Message was sent under, chosen explicitly by the sender at
 * composition (never inferred from participant composition): OWNER for "en
 * tant que copropriétaire", BOARD for "en tant que [rôle] du bureau" of the
 * conversation's property. Only meaningful choice on a GROUP conversation
 * where the sender holds both roles on that property - BOARD_PRIVATE and
 * BROADCAST messages are always BOARD (see Conversation/SendMessageService),
 * enforced server-side regardless of what the client sends.
 */
public enum SenderIdentity {
    OWNER,
    BOARD
}
