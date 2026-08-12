package com.architek.oikos.notification.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.architek.oikos.notification.application.command.CreateNotificationCommand;
import com.architek.oikos.notification.application.port.out.PushNotificationPort;
import com.architek.oikos.notification.domain.model.Notification;
import com.architek.oikos.notification.domain.model.NotificationType;
import com.architek.oikos.notification.domain.repository.DevicePushTokenRepository;
import com.architek.oikos.notification.domain.repository.NotificationRepository;
import com.architek.oikos.notification.domain.valueobject.NotificationId;
import com.architek.oikos.shared.domain.valueobject.EntityId;

@ExtendWith(MockitoExtension.class)
class CreateNotificationServiceTest {

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private DevicePushTokenRepository devicePushTokenRepository;

    @Mock
    private PushNotificationPort pushNotificationPort;

    private CreateNotificationService newService() {
        return new CreateNotificationService(notificationRepository, devicePushTokenRepository, pushNotificationPort);
    }

    @Test
    void creates_and_saves_a_notification_for_its_recipient() {
        EntityId recipientUserId = EntityId.newId();
        EntityId propertyId = EntityId.newId();
        when(notificationRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(devicePushTokenRepository.findTokensByUserId(recipientUserId)).thenReturn(List.of());

        NotificationId id = newService().create(new CreateNotificationCommand(recipientUserId, propertyId,
                NotificationType.INSTALLMENT_OVERDUE, "Échéance en retard", "Votre échéance du 5 janvier est en retard.",
                "/property-ownership/installments"));

        assertThat(id).isNotNull();
    }

    @Test
    void the_saved_notification_carries_the_command_fields() {
        EntityId recipientUserId = EntityId.newId();
        when(notificationRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(devicePushTokenRepository.findTokensByUserId(recipientUserId)).thenReturn(List.of());

        newService().create(new CreateNotificationCommand(recipientUserId, null, NotificationType.GENERAL, "Titre", null, null));

        ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
        verify(notificationRepository).save(captor.capture());
        assertThat(captor.getValue().getRecipientUserId()).isEqualTo(recipientUserId);
        assertThat(captor.getValue().getType()).isEqualTo(NotificationType.GENERAL);
        assertThat(captor.getValue().isRead()).isFalse();
    }

    @Test
    void sends_a_push_when_the_recipient_has_registered_devices() {
        EntityId recipientUserId = EntityId.newId();
        when(notificationRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(devicePushTokenRepository.findTokensByUserId(recipientUserId)).thenReturn(List.of("ExponentPushToken[abc]"));

        newService().create(new CreateNotificationCommand(recipientUserId, null, NotificationType.GENERAL, "Titre", "Corps", null));

        verify(pushNotificationPort).sendPush(List.of("ExponentPushToken[abc]"), "Titre", "Corps", null);
    }

    @Test
    void a_push_failure_never_rolls_back_the_notification() {
        EntityId recipientUserId = EntityId.newId();
        when(notificationRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(devicePushTokenRepository.findTokensByUserId(recipientUserId)).thenReturn(List.of("ExponentPushToken[abc]"));
        org.mockito.Mockito.doThrow(new RuntimeException("Expo is down"))
                .when(pushNotificationPort).sendPush(any(), any(), any(), any());

        NotificationId id = newService().create(new CreateNotificationCommand(recipientUserId, null, NotificationType.GENERAL, "Titre", null, null));

        assertThat(id).isNotNull();
        verify(notificationRepository).save(any());
    }

    @Test
    void no_push_is_attempted_when_the_recipient_has_no_registered_device() {
        EntityId recipientUserId = EntityId.newId();
        when(notificationRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(devicePushTokenRepository.findTokensByUserId(recipientUserId)).thenReturn(List.of());

        newService().create(new CreateNotificationCommand(recipientUserId, null, NotificationType.GENERAL, "Titre", null, null));

        verify(pushNotificationPort, never()).sendPush(any(), any(), any(), any());
    }
}
