package com.architek.oikos.messaging.application.usecase;

import java.util.List;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.architek.oikos.messaging.application.dto.ConversationSummaryView;
import com.architek.oikos.messaging.application.dto.UnreadSummaryView;
import com.architek.oikos.messaging.application.port.in.GetUnreadSummaryUseCase;
import com.architek.oikos.messaging.application.query.GetUnreadSummaryQuery;

@Component
public class GetUnreadSummaryService implements GetUnreadSummaryUseCase {

    private static final int MAX_RECENT_UNREAD = 5;

    private final ConversationAggregator conversationAggregator;

    public GetUnreadSummaryService(ConversationAggregator conversationAggregator) {
        this.conversationAggregator = conversationAggregator;
    }

    @Override
    @Transactional(readOnly = true)
    public UnreadSummaryView getSummary(GetUnreadSummaryQuery query) {
        List<ConversationSummaryView> all = conversationAggregator.listAll(query.userId(), null);

        List<ConversationSummaryView> unread = all.stream().filter(view -> view.unreadCount() > 0).toList();
        long totalUnreadMessageCount = unread.stream().mapToLong(ConversationSummaryView::unreadCount).sum();
        List<ConversationSummaryView> recentUnread = unread.stream().limit(MAX_RECENT_UNREAD).toList();

        return new UnreadSummaryView(unread.size(), totalUnreadMessageCount, recentUnread);
    }
}
