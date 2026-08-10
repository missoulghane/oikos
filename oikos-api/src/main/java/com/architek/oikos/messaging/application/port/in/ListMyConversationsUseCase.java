package com.architek.oikos.messaging.application.port.in;

import com.architek.oikos.messaging.application.dto.ConversationSummaryView;
import com.architek.oikos.messaging.application.query.ListMyConversationsQuery;
import com.architek.oikos.shared.domain.pagination.Page;

public interface ListMyConversationsUseCase {

    Page<ConversationSummaryView> listConversations(ListMyConversationsQuery query);
}
