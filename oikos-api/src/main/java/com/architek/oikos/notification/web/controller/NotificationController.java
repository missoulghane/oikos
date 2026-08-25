package com.architek.oikos.notification.web.controller;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.architek.oikos.auth.infrastructure.security.UserPrincipal;
import com.architek.oikos.notification.application.command.MarkNotificationReadCommand;
import com.architek.oikos.notification.application.command.RegisterDevicePushTokenCommand;
import com.architek.oikos.notification.application.port.in.GetNotificationUseCase;
import com.architek.oikos.notification.application.port.in.GetUnreadNotificationCountUseCase;
import com.architek.oikos.notification.application.port.in.ListMyNotificationsUseCase;
import com.architek.oikos.notification.application.port.in.MarkNotificationReadUseCase;
import com.architek.oikos.notification.application.port.in.RegisterDevicePushTokenUseCase;
import com.architek.oikos.notification.application.query.GetNotificationQuery;
import com.architek.oikos.notification.application.query.GetUnreadNotificationCountQuery;
import com.architek.oikos.notification.application.query.ListMyNotificationsQuery;
import com.architek.oikos.notification.domain.valueobject.NotificationId;
import com.architek.oikos.notification.web.request.RegisterDevicePushTokenRequest;
import com.architek.oikos.notification.web.response.NotificationResponse;
import com.architek.oikos.notification.web.response.PagedNotificationResponse;
import com.architek.oikos.notification.web.response.UnreadNotificationCountResponse;
import com.architek.oikos.shared.domain.pagination.PageRequest;
import com.architek.oikos.shared.domain.valueobject.EntityId;

/**
 * GET /notifications/{id} is the individually consultable URL every
 * notification must have (arbitrage A3, GAP.md) - opening one from anywhere
 * (an email, a shared link) resolves it on its own, not just as a row inside
 * the paginated inbox below.
 */
@RestController
public class NotificationController {

    private final ListMyNotificationsUseCase listMyNotificationsUseCase;
    private final GetUnreadNotificationCountUseCase getUnreadNotificationCountUseCase;
    private final GetNotificationUseCase getNotificationUseCase;
    private final MarkNotificationReadUseCase markNotificationReadUseCase;
    private final RegisterDevicePushTokenUseCase registerDevicePushTokenUseCase;

    public NotificationController(ListMyNotificationsUseCase listMyNotificationsUseCase,
                                   GetUnreadNotificationCountUseCase getUnreadNotificationCountUseCase,
                                   GetNotificationUseCase getNotificationUseCase,
                                   MarkNotificationReadUseCase markNotificationReadUseCase,
                                   RegisterDevicePushTokenUseCase registerDevicePushTokenUseCase) {
        this.listMyNotificationsUseCase = listMyNotificationsUseCase;
        this.getUnreadNotificationCountUseCase = getUnreadNotificationCountUseCase;
        this.getNotificationUseCase = getNotificationUseCase;
        this.markNotificationReadUseCase = markNotificationReadUseCase;
        this.registerDevicePushTokenUseCase = registerDevicePushTokenUseCase;
    }

    @GetMapping("/users/me/notifications")
    public PagedNotificationResponse listMyNotifications(@RequestParam(defaultValue = "0") int page,
                                                           @RequestParam(defaultValue = "20") int size,
                                                           @RequestParam(defaultValue = "false") boolean unreadOnly,
                                                           Authentication authentication) {
        return PagedNotificationResponse.from(listMyNotificationsUseCase.listNotifications(
                new ListMyNotificationsQuery(currentUserId(authentication), unreadOnly, PageRequest.of(page, size))));
    }

    @GetMapping("/users/me/notifications/unread-count")
    public UnreadNotificationCountResponse unreadCount(Authentication authentication) {
        return new UnreadNotificationCountResponse(
                getUnreadNotificationCountUseCase.getUnreadCount(new GetUnreadNotificationCountQuery(currentUserId(authentication))));
    }

    /** Called by oikos-mobile after login (and after each app launch while a session is active) to (re-)register the device's Expo push token. */
    @PostMapping("/users/me/push-tokens")
    public ResponseEntity<Void> registerPushToken(@Valid @RequestBody RegisterDevicePushTokenRequest request,
                                                   Authentication authentication) {
        registerDevicePushTokenUseCase.register(new RegisterDevicePushTokenCommand(currentUserId(authentication), request.expoPushToken()));
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("@propertyAccess.isNotificationOwner(authentication, #id)")
    @GetMapping("/notifications/{id}")
    public NotificationResponse get(@PathVariable String id) {
        return NotificationResponse.from(getNotificationUseCase.getNotification(new GetNotificationQuery(NotificationId.of(id))));
    }

    @PreAuthorize("@propertyAccess.isNotificationOwner(authentication, #id)")
    @PostMapping("/notifications/{id}/read")
    public ResponseEntity<Void> markRead(@PathVariable String id) {
        markNotificationReadUseCase.markRead(new MarkNotificationReadCommand(NotificationId.of(id)));
        return ResponseEntity.noContent().build();
    }

    private EntityId currentUserId(Authentication authentication) {
        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
        return EntityId.of(principal.getUserId());
    }
}
