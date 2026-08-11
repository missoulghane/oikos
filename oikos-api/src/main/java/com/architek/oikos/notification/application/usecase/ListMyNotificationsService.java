package com.architek.oikos.notification.application.usecase;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.architek.oikos.notification.application.dto.NotificationView;
import com.architek.oikos.notification.application.port.in.ListMyNotificationsUseCase;
import com.architek.oikos.notification.application.query.ListMyNotificationsQuery;
import com.architek.oikos.notification.domain.repository.NotificationRepository;
import com.architek.oikos.shared.domain.pagination.Page;

/** Real DB-level pagination, same rationale as ListMyMessageDraftsService: a
 * notification is a single flat table with no cross-source aggregation to
 * do (unlike ConversationAggregator, which merges GROUP/BROADCAST/
 * BOARD_PRIVATE from several queries in memory). Scope filtering ("Tout" /
 * "Mes biens" / "Mes mandats") happens client-side against the caller's own
 * roleByProperty, already available from GET /users/me - see
 * NotificationsListPage - rather than duplicating that role classification
 * here. */
@Component
public class ListMyNotificationsService implements ListMyNotificationsUseCase {

    private final NotificationRepository notificationRepository;

    public ListMyNotificationsService(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<NotificationView> listNotifications(ListMyNotificationsQuery query) {
        return notificationRepository.findByRecipientUserId(query.userId(), query.pageRequest()).map(NotificationViewMapper::toView);
    }
}
