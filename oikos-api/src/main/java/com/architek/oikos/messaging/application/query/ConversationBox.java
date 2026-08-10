package com.architek.oikos.messaging.application.query;

/**
 * Which side of a conversation the caller is asking about: RECEIVED (they
 * received at least one message in it, i.e. someone else posted) or SENT
 * (they posted at least one message in it themselves) - see
 * ConversationAggregator.listAll. A back-and-forth conversation matches
 * both; a message the caller sent that nobody replied to yet only matches
 * SENT; one they received but never replied to only matches RECEIVED.
 */
public enum ConversationBox {
    RECEIVED,
    SENT
}
