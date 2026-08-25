package com.architek.oikos.notification.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.architek.oikos.notification.application.dto.NotificationView;
import com.architek.oikos.notification.application.query.ListMyNotificationsQuery;
import com.architek.oikos.notification.domain.model.Notification;
import com.architek.oikos.notification.domain.model.NotificationType;
import com.architek.oikos.notification.domain.repository.NotificationRepository;
import com.architek.oikos.notification.domain.valueobject.NotificationId;
import com.architek.oikos.shared.domain.pagination.Page;
import com.architek.oikos.shared.domain.pagination.PageRequest;
import com.architek.oikos.shared.domain.valueobject.EntityId;

@ExtendWith(MockitoExtension.class)
class ListMyNotificationsServiceTest {

    @Mock
    private NotificationRepository notificationRepository;

    private ListMyNotificationsService newService() {
        return new ListMyNotificationsService(notificationRepository);
    }

    @Test
    void lists_the_callers_notifications_mapped_to_views() {
        EntityId userId = EntityId.newId();
        PageRequest pageRequest = PageRequest.of(0, 20);
        Notification notification = Notification.reconstruct(NotificationId.newId(), userId, EntityId.newId(),
                NotificationType.GENERAL_MEETING_CALLED, "AG convoquée", "Assemblée générale le 12 mars.", "/dashboard",
                null, java.time.Instant.EPOCH);
        when(notificationRepository.findByRecipientUserId(userId, false, pageRequest))
                .thenReturn(Page.of(java.util.List.of(notification), 0, 20, 1));

        Page<NotificationView> result = newService().listNotifications(new ListMyNotificationsQuery(userId, pageRequest));

        assertThat(result.content()).hasSize(1);
        NotificationView view = result.content().get(0);
        assertThat(view.title()).isEqualTo("AG convoquée");
        assertThat(view.read()).isFalse();
        assertThat(view.recipientUserId()).isEqualTo(userId);
    }

    // Ce que demande la cloche du header : elle n'affiche que le non-lu, et
    // c'est le dépôt qui restreint - pas un filtrage de la page reçue, qui
    // rendrait 5 lignes dont 2 visibles.
    @Test
    void hands_the_unread_only_flag_down_to_the_repository() {
        EntityId userId = EntityId.newId();
        PageRequest pageRequest = PageRequest.of(0, 5);
        when(notificationRepository.findByRecipientUserId(userId, true, pageRequest))
                .thenReturn(Page.of(java.util.List.of(), 0, 5, 0));

        newService().listNotifications(new ListMyNotificationsQuery(userId, true, pageRequest));

        verify(notificationRepository).findByRecipientUserId(userId, true, pageRequest);
    }
}
