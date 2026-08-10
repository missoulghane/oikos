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
import com.architek.oikos.messaging.application.dto.UnreadSummaryView;
import com.architek.oikos.messaging.application.query.GetUnreadSummaryQuery;
import com.architek.oikos.messaging.domain.model.ConversationType;
import com.architek.oikos.messaging.domain.valueobject.ConversationId;
import com.architek.oikos.shared.domain.valueobject.EntityId;

@ExtendWith(MockitoExtension.class)
class GetUnreadSummaryServiceTest {

    @Mock
    private ConversationAggregator conversationAggregator;

    private GetUnreadSummaryService newService() {
        return new GetUnreadSummaryService(conversationAggregator);
    }

    private static ConversationSummaryView summary(long unreadCount) {
        return new ConversationSummaryView(ConversationId.newId(), ConversationType.BROADCAST, EntityId.newId(), "Copro",
                null, List.of(), null, Instant.EPOCH, unreadCount, 1);
    }

    @Test
    void computes_counts_and_caps_recent_unread_at_5() {
        EntityId userId = EntityId.newId();
        List<ConversationSummaryView> all = List.of(
                summary(3), summary(1), summary(0), summary(2), summary(4), summary(5), summary(0));
        when(conversationAggregator.listAll(userId, null)).thenReturn(all);

        UnreadSummaryView summaryView = newService().getSummary(new GetUnreadSummaryQuery(userId));

        assertThat(summaryView.unreadConversationCount()).isEqualTo(5);
        assertThat(summaryView.totalUnreadMessageCount()).isEqualTo(3 + 1 + 2 + 4 + 5);
        assertThat(summaryView.recentUnread()).hasSize(5);
        assertThat(summaryView.recentUnread()).allMatch(view -> view.unreadCount() > 0);
    }

    @Test
    void returns_zero_counts_when_nothing_is_unread() {
        EntityId userId = EntityId.newId();
        when(conversationAggregator.listAll(userId, null)).thenReturn(List.of(summary(0), summary(0)));

        UnreadSummaryView summaryView = newService().getSummary(new GetUnreadSummaryQuery(userId));

        assertThat(summaryView.unreadConversationCount()).isZero();
        assertThat(summaryView.totalUnreadMessageCount()).isZero();
        assertThat(summaryView.recentUnread()).isEmpty();
    }
}
