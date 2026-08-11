package com.architek.oikos.messaging.domain.model;

/**
 * GROUP: composed by the sender to 1..N applicatively-chosen recipients
 * (2..N total participantUserIds including the sender) - never reused:
 * composing to the same set of people twice always creates a brand-new
 * conversation (Outlook-style "New message"), see Conversation's javadoc.
 * BOARD_PRIVATE: a subject-bearing thread visible only to the property's
 * current staff (board/manager) - like GROUP, several distinct threads can
 * exist per property, but like BROADCAST its membership is resolved
 * dynamically at read time rather than stored (an ex-board-member loses
 * access immediately, a new one sees the history) - see Conversation.
 * BROADCAST: one persistent announcement channel per property, from the
 * board/manager tier to every current member - never a one-off broadcast
 * message, see Conversation javadoc.
 */
public enum ConversationType {
    GROUP,
    BOARD_PRIVATE,
    BROADCAST
}
