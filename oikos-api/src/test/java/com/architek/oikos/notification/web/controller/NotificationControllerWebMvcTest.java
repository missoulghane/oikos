package com.architek.oikos.notification.web.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.architek.oikos.auth.infrastructure.security.JwtService;
import com.architek.oikos.notification.application.command.MarkNotificationReadCommand;
import com.architek.oikos.notification.application.command.RegisterDevicePushTokenCommand;
import com.architek.oikos.notification.application.dto.NotificationView;
import com.architek.oikos.notification.application.port.in.GetNotificationUseCase;
import com.architek.oikos.notification.application.port.in.GetUnreadNotificationCountUseCase;
import com.architek.oikos.notification.application.port.in.ListMyNotificationsUseCase;
import com.architek.oikos.notification.application.port.in.MarkNotificationReadUseCase;
import com.architek.oikos.notification.application.port.in.RegisterDevicePushTokenUseCase;
import com.architek.oikos.notification.application.query.GetNotificationQuery;
import com.architek.oikos.notification.domain.model.NotificationType;
import com.architek.oikos.notification.domain.valueobject.NotificationId;
import com.architek.oikos.shared.domain.valueobject.EntityId;
import com.architek.oikos.testsupport.WebSecuritySliceTestConfiguration;
import com.architek.oikos.user.application.port.in.GetUserAccessUseCase;

@WebMvcTest(controllers = NotificationController.class)
@Import(WebSecuritySliceTestConfiguration.class)
class NotificationControllerWebMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private GetUserAccessUseCase getUserAccessUseCase;

    @MockitoBean
    private ListMyNotificationsUseCase listMyNotificationsUseCase;

    @MockitoBean
    private GetUnreadNotificationCountUseCase getUnreadNotificationCountUseCase;

    @MockitoBean
    private GetNotificationUseCase getNotificationUseCase;

    @MockitoBean
    private MarkNotificationReadUseCase markNotificationReadUseCase;

    @MockitoBean
    private RegisterDevicePushTokenUseCase registerDevicePushTokenUseCase;

    private String bearerToken(UUID userId, String... authorities) {
        return "Bearer " + jwtService.generateAccessToken(EntityId.of(userId), Set.of(authorities));
    }

    @Test
    void anonymous_request_is_rejected_with_401() throws Exception {
        mockMvc.perform(get("/api/v1/users/me/notifications"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void the_recipient_can_open_their_own_notification() throws Exception {
        UUID userId = UUID.randomUUID();
        NotificationId notificationId = NotificationId.newId();
        when(getNotificationUseCase.getNotification(new GetNotificationQuery(notificationId)))
                .thenReturn(new NotificationView(notificationId, EntityId.of(userId), null, NotificationType.GENERAL,
                        "Titre", null, null, false, Instant.EPOCH));

        mockMvc.perform(get("/api/v1/notifications/" + notificationId)
                        .header("Authorization", bearerToken(userId, "ROLE_USER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Titre"));
    }

    @Test
    void a_different_user_cannot_open_someone_else_s_notification() throws Exception {
        UUID owner = UUID.randomUUID();
        UUID stranger = UUID.randomUUID();
        NotificationId notificationId = NotificationId.newId();
        when(getNotificationUseCase.getNotification(new GetNotificationQuery(notificationId)))
                .thenReturn(new NotificationView(notificationId, EntityId.of(owner), null, NotificationType.GENERAL,
                        "Titre", null, null, false, Instant.EPOCH));

        mockMvc.perform(get("/api/v1/notifications/" + notificationId)
                        .header("Authorization", bearerToken(stranger, "ROLE_USER")))
                .andExpect(status().isForbidden());
    }

    @Test
    void the_recipient_can_mark_their_own_notification_read() throws Exception {
        UUID userId = UUID.randomUUID();
        NotificationId notificationId = NotificationId.newId();
        when(getNotificationUseCase.getNotification(new GetNotificationQuery(notificationId)))
                .thenReturn(new NotificationView(notificationId, EntityId.of(userId), null, NotificationType.GENERAL,
                        "Titre", null, null, false, Instant.EPOCH));

        mockMvc.perform(post("/api/v1/notifications/" + notificationId + "/read")
                        .header("Authorization", bearerToken(userId, "ROLE_USER")))
                .andExpect(status().isNoContent());

        verify(markNotificationReadUseCase).markRead(new MarkNotificationReadCommand(notificationId));
    }

    @Test
    void listing_my_notifications_returns_the_use_case_s_page() throws Exception {
        UUID userId = UUID.randomUUID();
        when(listMyNotificationsUseCase.listNotifications(any()))
                .thenReturn(com.architek.oikos.shared.domain.pagination.Page.of(java.util.List.of(), 0, 20, 0));

        mockMvc.perform(get("/api/v1/users/me/notifications")
                        .header("Authorization", bearerToken(userId, "ROLE_USER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(0));
    }

    @Test
    void the_current_user_can_register_a_device_push_token() throws Exception {
        UUID userId = UUID.randomUUID();

        mockMvc.perform(post("/api/v1/users/me/push-tokens")
                        .header("Authorization", bearerToken(userId, "ROLE_USER"))
                        .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                        .content("{\"expoPushToken\":\"ExponentPushToken[abc]\"}"))
                .andExpect(status().isNoContent());

        verify(registerDevicePushTokenUseCase)
                .register(new RegisterDevicePushTokenCommand(EntityId.of(userId), "ExponentPushToken[abc]"));
    }
}
