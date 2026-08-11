package com.architek.oikos.notification.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.architek.oikos.notification.application.dto.NotificationView;
import com.architek.oikos.notification.application.query.GetNotificationQuery;
import com.architek.oikos.notification.domain.exception.NotificationNotFoundException;
import com.architek.oikos.notification.domain.model.Notification;
import com.architek.oikos.notification.domain.model.NotificationType;
import com.architek.oikos.notification.domain.repository.NotificationRepository;
import com.architek.oikos.notification.domain.valueobject.NotificationId;
import com.architek.oikos.shared.domain.valueobject.EntityId;

@ExtendWith(MockitoExtension.class)
class GetNotificationServiceTest {

    @Mock
    private NotificationRepository notificationRepository;

    private GetNotificationService newService() {
        return new GetNotificationService(notificationRepository);
    }

    @Test
    void returns_the_notification_as_a_view() {
        NotificationId id = NotificationId.newId();
        Notification notification = Notification.create(id, EntityId.newId(), null, NotificationType.GENERAL, "Titre", null, null);
        when(notificationRepository.findById(id)).thenReturn(Optional.of(notification));

        NotificationView view = newService().getNotification(new GetNotificationQuery(id));

        assertThat(view.id()).isEqualTo(id);
        assertThat(view.title()).isEqualTo("Titre");
    }

    @Test
    void an_unknown_id_is_rejected_with_not_found() {
        NotificationId id = NotificationId.newId();
        when(notificationRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> newService().getNotification(new GetNotificationQuery(id)))
                .isInstanceOf(NotificationNotFoundException.class);
    }
}
