package com.architek.oikos.messaging.application.query;

import com.architek.oikos.shared.domain.pagination.PageRequest;
import com.architek.oikos.shared.domain.valueobject.EntityId;

/**
 * search filters by any other participant's name (GROUP) or property name
 * (BROADCAST), case-insensitive substring; null means no filter. box
 * restricts to conversations the caller has received into (RECEIVED) or
 * sent into (SENT) at least one message; null means unfiltered (every
 * conversation the caller participates in, the original behavior).
 */
public record ListMyConversationsQuery(EntityId userId, PageRequest pageRequest, String search, ConversationBox box) {
}
