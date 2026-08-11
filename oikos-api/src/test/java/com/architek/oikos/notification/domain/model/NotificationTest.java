package com.architek.oikos.notification.domain.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Instant;

import org.junit.jupiter.api.Test;

import com.architek.oikos.notification.domain.valueobject.NotificationId;
import com.architek.oikos.shared.domain.valueobject.EntityId;

class NotificationTest {

    @Test
    void creating_a_notification_starts_unread_with_no_created_date_yet() {
        Notification notification = Notification.create(NotificationId.newId(), EntityId.newId(), EntityId.newId(),
                NotificationType.GENERAL, "Titre", "Corps", "/dashboard");

        assertThat(notification.isRead()).isFalse();
        assertThat(notification.getReadAt()).isNull();
        assertThat(notification.getCreatedDate()).isNull();
        assertThat(notification.getTitle()).isEqualTo("Titre");
    }

    @Test
    void a_notification_may_have_no_property_body_or_link() {
        Notification notification =
                Notification.create(NotificationId.newId(), EntityId.newId(), null, NotificationType.GENERAL, "Titre", null, null);

        assertThat(notification.getPropertyId()).isNull();
        assertThat(notification.getBody()).isNull();
        assertThat(notification.getLinkPath()).isNull();
    }

    @Test
    void a_blank_title_is_rejected() {
        assertThatThrownBy(() -> Notification.create(NotificationId.newId(), EntityId.newId(), null,
                NotificationType.GENERAL, "   ", null, null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void a_title_over_200_characters_is_rejected() {
        String tooLong = "a".repeat(201);

        assertThatThrownBy(() -> Notification.create(NotificationId.newId(), EntityId.newId(), null,
                NotificationType.GENERAL, tooLong, null, null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void marking_read_sets_the_read_timestamp() {
        Notification notification = Notification.create(NotificationId.newId(), EntityId.newId(), null,
                NotificationType.GENERAL, "Titre", null, null);
        Instant now = Instant.parse("2026-01-01T10:00:00Z");

        Notification read = notification.markRead(now);

        assertThat(read.isRead()).isTrue();
        assertThat(read.getReadAt()).isEqualTo(now);
    }

    @Test
    void marking_an_already_read_notification_read_again_keeps_the_original_timestamp() {
        Notification notification = Notification.create(NotificationId.newId(), EntityId.newId(), null,
                NotificationType.GENERAL, "Titre", null, null);
        Instant firstRead = Instant.parse("2026-01-01T10:00:00Z");
        Notification read = notification.markRead(firstRead);

        Notification readAgain = read.markRead(Instant.parse("2026-01-02T10:00:00Z"));

        assertThat(readAgain.getReadAt()).isEqualTo(firstRead);
    }

    @Test
    void two_notifications_with_the_same_id_are_equal() {
        NotificationId id = NotificationId.newId();
        Notification first = Notification.create(id, EntityId.newId(), null, NotificationType.GENERAL, "Titre", null, null);
        Notification second = Notification.create(id, EntityId.newId(), null, NotificationType.GENERAL, "Autre", null, null);

        assertThat(first).isEqualTo(second);
        assertThat(first).hasSameHashCodeAs(second);
    }
}
