package com.architek.oikos.messaging.application.port.in;

import com.architek.oikos.messaging.application.dto.ConversationView;
import com.architek.oikos.messaging.application.query.GetConversationQuery;

/** Used by PropertyAccessEvaluator.isConversationParticipant, in addition to messaging's own web layer. */
public interface GetConversationUseCase {

    ConversationView getConversation(GetConversationQuery query);
}
