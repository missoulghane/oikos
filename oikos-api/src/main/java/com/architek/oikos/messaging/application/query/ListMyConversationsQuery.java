package com.architek.oikos.messaging.application.query;

import com.architek.oikos.shared.domain.pagination.PageRequest;
import com.architek.oikos.shared.domain.valueobject.EntityId;

/**
 * search filters by any other participant's name (GROUP) or property name
 * (BROADCAST), case-insensitive substring; null means no filter. box
 * restricts to conversations the caller has received into (RECEIVED) or
 * sent into (SENT) at least one message; null means unfiltered (every
 * conversation the caller participates in, the original behavior). readState
 * restricts to the conversations holding unread messages, or to those holding
 * none; null means unfiltered.
 */
public record ListMyConversationsQuery(EntityId userId, PageRequest pageRequest, String search, ConversationBox box,
                                        ConversationReadState readState) {

    public ListMyConversationsQuery(EntityId userId, PageRequest pageRequest, String search, ConversationBox box) {
        this(userId, pageRequest, search, box, null);
    }
}
