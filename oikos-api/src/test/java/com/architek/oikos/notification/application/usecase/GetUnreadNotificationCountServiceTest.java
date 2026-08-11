package com.architek.oikos.notification.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.architek.oikos.notification.application.query.GetUnreadNotificationCountQuery;
import com.architek.oikos.notification.domain.repository.NotificationRepository;
import com.architek.oikos.shared.domain.valueobject.EntityId;

@ExtendWith(MockitoExtension.class)
class GetUnreadNotificationCountServiceTest {

    @Mock
    private NotificationRepository notificationRepository;

    private GetUnreadNotificationCountService newService() {
        return new GetUnreadNotificationCountService(notificationRepository);
    }

    @Test
    void returns_the_repository_s_unread_count_for_the_caller() {
        EntityId userId = EntityId.newId();
        when(notificationRepository.countUnreadByRecipientUserId(userId)).thenReturn(3L);

        long count = newService().getUnreadCount(new GetUnreadNotificationCountQuery(userId));

        assertThat(count).isEqualTo(3L);
    }
}
