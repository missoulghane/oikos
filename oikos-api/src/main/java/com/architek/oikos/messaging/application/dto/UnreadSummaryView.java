package com.architek.oikos.messaging.application.dto;

import java.util.List;

/** Feeds the notification bell: recentUnread holds at most the 5 most recently active conversations that have unread messages. */
public record UnreadSummaryView(long unreadConversationCount, long totalUnreadMessageCount,
                                 List<ConversationSummaryView> recentUnread) {
}
