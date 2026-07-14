package com.architek.oikos.user.web.request;

import jakarta.validation.constraints.NotNull;

public record ChangeUserStatusRequest(@NotNull Boolean enabled) {
}
