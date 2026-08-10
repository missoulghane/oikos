package com.architek.oikos.messaging.domain.model;

/**
 * GROUP: composed by the sender to 1..N applicatively-chosen recipients
 * (2..N total participantUserIds including the sender) - never reused:
 * composing to the same set of people twice always creates a brand-new
 * conversation (Outlook-style "New message"), see Conversation's javadoc.
 * BROADCAST: one persistent announcement channel per property, from the
 * board/manager tier to every current member - never a one-off broadcast
 * message, see Conversation javadoc.
 */
public enum ConversationType {
    GROUP,
    BROADCAST
}
