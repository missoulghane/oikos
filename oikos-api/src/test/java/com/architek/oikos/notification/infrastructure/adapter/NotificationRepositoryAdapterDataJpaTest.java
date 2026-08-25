package com.architek.oikos.notification.infrastructure.adapter;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;

import com.architek.oikos.notification.domain.model.Notification;
import com.architek.oikos.notification.domain.model.NotificationType;
import com.architek.oikos.notification.domain.valueobject.NotificationId;
import com.architek.oikos.notification.infrastructure.mapper.NotificationPersistenceMapperImpl;
import com.architek.oikos.shared.domain.pagination.Page;
import com.architek.oikos.shared.domain.pagination.PageRequest;
import com.architek.oikos.shared.domain.valueobject.EntityId;
import com.architek.oikos.shared.infrastructure.configuration.JpaAuditingConfiguration;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({NotificationRepositoryAdapter.class, NotificationPersistenceMapperImpl.class, JpaAuditingConfiguration.class})
class NotificationRepositoryAdapterDataJpaTest {

    @Autowired
    private NotificationRepositoryAdapter adapter;

    @Test
    void saves_and_reloads_a_notification() {
        EntityId userId = EntityId.newId();
        EntityId propertyId = EntityId.newId();
        Notification saved = adapter.save(Notification.create(NotificationId.newId(), userId, propertyId,
                NotificationType.INSTALLMENT_OVERDUE, "Échéance en retard", "Corps du message", "/dashboard"));

        Notification reloaded = adapter.findById(saved.getId()).orElseThrow();

        assertThat(reloaded.getRecipientUserId()).isEqualTo(userId);
        assertThat(reloaded.getPropertyId()).isEqualTo(propertyId);
        assertThat(reloaded.getTitle()).isEqualTo("Échéance en retard");
        assertThat(reloaded.isRead()).isFalse();
        assertThat(reloaded.getCreatedDate()).isNotNull();
    }

    @Test
    void a_notification_with_no_property_reloads_with_a_null_property_id() {
        Notification saved =
                adapter.save(Notification.create(NotificationId.newId(), EntityId.newId(), null, NotificationType.GENERAL, "Titre", null, null));

        Notification reloaded = adapter.findById(saved.getId()).orElseThrow();

        assertThat(reloaded.getPropertyId()).isNull();
    }

    @Test
    void lists_a_user_s_notifications_most_recent_first() {
        EntityId userId = EntityId.newId();
        Notification first = adapter.save(Notification.create(NotificationId.newId(), userId, null, NotificationType.GENERAL, "Premier", null, null));
        Notification second = adapter.save(Notification.create(NotificationId.newId(), userId, null, NotificationType.GENERAL, "Second", null, null));
        adapter.save(Notification.create(NotificationId.newId(), EntityId.newId(), null, NotificationType.GENERAL, "Autre utilisateur", null, null));

        Page<Notification> page = adapter.findByRecipientUserId(userId, false, PageRequest.of(0, 20));

        assertThat(page.content()).extracting(Notification::getId).containsExactlyInAnyOrder(first.getId(), second.getId());
        assertThat(page.totalElements()).isEqualTo(2);
    }

    // La cloche liste le non-lu : une notification ouverte en sort, et le total
    // suit - sans quoi la pagination annoncerait des lignes qu'elle ne rend pas.
    @Test
    void lists_only_the_unread_ones_when_asked() {
        EntityId userId = EntityId.newId();
        Notification unread = adapter.save(Notification.create(NotificationId.newId(), userId, null, NotificationType.GENERAL, "Non lu", null, null));
        Notification read = adapter.save(Notification.create(NotificationId.newId(), userId, null, NotificationType.GENERAL, "Lu", null, null));
        adapter.save(read.markRead(java.time.Instant.now()));

        Page<Notification> page = adapter.findByRecipientUserId(userId, true, PageRequest.of(0, 20));

        assertThat(page.content()).extracting(Notification::getId).containsExactly(unread.getId());
        assertThat(page.totalElements()).isEqualTo(1);
    }

    @Test
    void counts_only_unread_notifications_for_the_user() {
        EntityId userId = EntityId.newId();
        Notification unread = adapter.save(Notification.create(NotificationId.newId(), userId, null, NotificationType.GENERAL, "Non lu", null, null));
        Notification toMarkRead = adapter.save(Notification.create(NotificationId.newId(), userId, null, NotificationType.GENERAL, "Sera lu", null, null));
        adapter.save(toMarkRead.markRead(java.time.Instant.now()));

        long unreadCount = adapter.countUnreadByRecipientUserId(userId);

        assertThat(unreadCount).isEqualTo(1);
        assertThat(unread.isRead()).isFalse();
    }
}
