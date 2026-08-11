package com.architek.oikos.notification.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.architek.oikos.notification.application.command.MarkNotificationReadCommand;
import com.architek.oikos.notification.domain.exception.NotificationNotFoundException;
import com.architek.oikos.notification.domain.model.Notification;
import com.architek.oikos.notification.domain.model.NotificationType;
import com.architek.oikos.notification.domain.repository.NotificationRepository;
import com.architek.oikos.notification.domain.valueobject.NotificationId;
import com.architek.oikos.shared.domain.valueobject.EntityId;

@ExtendWith(MockitoExtension.class)
class MarkNotificationReadServiceTest {

    @Mock
    private NotificationRepository notificationRepository;

    private MarkNotificationReadService newService(Instant now) {
        return new MarkNotificationReadService(notificationRepository, Clock.fixed(now, ZoneOffset.UTC));
    }

    @Test
    void marks_the_notification_read_at_the_current_time() {
        NotificationId id = NotificationId.newId();
        Notification notification = Notification.create(id, EntityId.newId(), null, NotificationType.GENERAL, "Titre", null, null);
        when(notificationRepository.findById(id)).thenReturn(Optional.of(notification));
        when(notificationRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        Instant now = Instant.parse("2026-01-01T10:00:00Z");

        newService(now).markRead(new MarkNotificationReadCommand(id));

        ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
        verify(notificationRepository).save(captor.capture());
        assertThat(captor.getValue().isRead()).isTrue();
        assertThat(captor.getValue().getReadAt()).isEqualTo(now);
    }

    @Test
    void an_unknown_id_is_rejected_with_not_found() {
        NotificationId id = NotificationId.newId();
        when(notificationRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> newService(Instant.EPOCH).markRead(new MarkNotificationReadCommand(id)))
                .isInstanceOf(NotificationNotFoundException.class);
    }
}
