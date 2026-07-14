package com.architek.oikos.user.application.command;

import com.architek.oikos.user.domain.valueobject.UserId;

public record ChangeUserStatusCommand(UserId id, boolean enabled) {
}
