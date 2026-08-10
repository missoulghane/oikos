package com.architek.oikos.messaging.web.response;

import java.util.List;

import com.architek.oikos.messaging.application.dto.UnreadSummaryView;

public record UnreadSummaryResponse(long unreadConversationCount, long totalUnreadMessageCount,
                                     List<ConversationSummaryResponse> recentUnread) {

    public static UnreadSummaryResponse from(UnreadSummaryView view) {
        List<ConversationSummaryResponse> recentUnread = view.recentUnread().stream().map(ConversationSummaryResponse::from).toList();
        return new UnreadSummaryResponse(view.unreadConversationCount(), view.totalUnreadMessageCount(), recentUnread);
    }
}
