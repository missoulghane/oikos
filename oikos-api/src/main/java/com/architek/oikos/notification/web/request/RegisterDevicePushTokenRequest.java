package com.architek.oikos.notification.web.request;

import jakarta.validation.constraints.NotBlank;

public record RegisterDevicePushTokenRequest(@NotBlank String expoPushToken) {
}
