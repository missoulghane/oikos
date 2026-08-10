package com.architek.oikos.messaging.application.usecase;

import java.util.List;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.architek.oikos.messaging.application.dto.ConversationSummaryView;
import com.architek.oikos.messaging.application.port.in.ListMyConversationsUseCase;
import com.architek.oikos.messaging.application.query.ListMyConversationsQuery;
import com.architek.oikos.shared.domain.pagination.Page;

/**
 * Pagination applies on top of the fully-aggregated, already-sorted list from
 * ConversationAggregator (in-memory paging, same pattern as
 * ListContactsByPropertyService).
 */
@Component
public class ListMyConversationsService implements ListMyConversationsUseCase {

    private final ConversationAggregator conversationAggregator;

    public ListMyConversationsService(ConversationAggregator conversationAggregator) {
        this.conversationAggregator = conversationAggregator;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ConversationSummaryView> listConversations(ListMyConversationsQuery query) {
        List<ConversationSummaryView> all = conversationAggregator.listAll(query.userId(), query.search());

        int pageSize = query.pageRequest().pageSize();
        int fromIndex = Math.min(query.pageRequest().pageNumber() * pageSize, all.size());
        int toIndex = Math.min(fromIndex + pageSize, all.size());

        return Page.of(all.subList(fromIndex, toIndex), query.pageRequest().pageNumber(), pageSize, all.size());
    }
}
