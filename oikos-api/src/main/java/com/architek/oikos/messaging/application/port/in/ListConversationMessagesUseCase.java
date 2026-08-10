package com.architek.oikos.messaging.application.port.in;

import com.architek.oikos.messaging.application.dto.MessageView;
import com.architek.oikos.messaging.application.query.ListConversationMessagesQuery;
import com.architek.oikos.shared.domain.pagination.Page;

public interface ListConversationMessagesUseCase {

    Page<MessageView> listMessages(ListConversationMessagesQuery query);
}
