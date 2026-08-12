package com.architek.oikos.notification.application.port.out;

import java.util.List;

public interface PushNotificationPort {

    /**
     * Best-effort: implementations must not throw on delivery failure -
     * CreateNotificationService already wraps this call so a push failure
     * never rolls back the Notification row it accompanies, but adapters
     * should still swallow/log their own errors rather than relying solely
     * on that caller-side guard.
     */
    void sendPush(List<String> expoPushTokens, String title, String body, String linkPath);
}
