package com.architek.oikos.messaging.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.architek.oikos.messaging.application.dto.ConversationSummaryView;
import com.architek.oikos.messaging.application.query.ConversationBox;
import com.architek.oikos.messaging.application.query.ConversationReadState;
import com.architek.oikos.messaging.application.query.ListMyConversationsQuery;
import com.architek.oikos.messaging.domain.model.ConversationType;
import com.architek.oikos.messaging.domain.valueobject.ConversationId;
import com.architek.oikos.shared.domain.pagination.Page;
import com.architek.oikos.shared.domain.pagination.PageRequest;
import com.architek.oikos.shared.domain.valueobject.EntityId;

@ExtendWith(MockitoExtension.class)
class ListMyConversationsServiceTest {

    @Mock
    private ConversationAggregator conversationAggregator;

    private ListMyConversationsService newService() {
        return new ListMyConversationsService(conversationAggregator);
    }

    private static ConversationSummaryView summary(EntityId propertyId) {
        return new ConversationSummaryView(ConversationId.newId(), ConversationType.BROADCAST, propertyId, "Copro",
                null, null, List.of(), null, Instant.EPOCH, 0, 1);
    }

    @Test
    void paginates_the_aggregated_list_in_memory() {
        EntityId userId = EntityId.newId();
        List<ConversationSummaryView> all = List.of(summary(EntityId.newId()), summary(EntityId.newId()), summary(EntityId.newId()));
        when(conversationAggregator.listAll(userId, null, null, null)).thenReturn(all);

        Page<ConversationSummaryView> page = newService().listConversations(new ListMyConversationsQuery(userId, PageRequest.of(0, 2), null, null));

        assertThat(page.content()).hasSize(2);
        assertThat(page.totalElements()).isEqualTo(3);
        assertThat(page.totalPages()).isEqualTo(2);
    }

    @Test
    void returns_an_empty_page_past_the_end_of_the_list() {
        EntityId userId = EntityId.newId();
        when(conversationAggregator.listAll(userId, null, null, null)).thenReturn(List.of(summary(EntityId.newId())));

        Page<ConversationSummaryView> page = newService().listConversations(new ListMyConversationsQuery(userId, PageRequest.of(5, 20), null, null));

        assertThat(page.content()).isEmpty();
        assertThat(page.totalElements()).isEqualTo(1);
    }

    @Test
    void forwards_the_box_filter_to_the_aggregator() {
        EntityId userId = EntityId.newId();
        when(conversationAggregator.listAll(userId, null, ConversationBox.SENT, null)).thenReturn(List.of(summary(EntityId.newId())));

        Page<ConversationSummaryView> page = newService()
                .listConversations(new ListMyConversationsQuery(userId, PageRequest.of(0, 20), null, ConversationBox.SENT));

        assertThat(page.content()).hasSize(1);
    }

    @Test
    void forwards_the_read_state_filter_to_the_aggregator() {
        EntityId userId = EntityId.newId();
        when(conversationAggregator.listAll(userId, null, null, ConversationReadState.UNREAD))
                .thenReturn(List.of(summary(EntityId.newId())));

        Page<ConversationSummaryView> page = newService().listConversations(
                new ListMyConversationsQuery(userId, PageRequest.of(0, 20), null, null, ConversationReadState.UNREAD));

        assertThat(page.content()).hasSize(1);
    }
}
