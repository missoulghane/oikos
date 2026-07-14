package com.architek.oikos.user.application.command;

import com.architek.oikos.user.domain.valueobject.UserId;

public record DeleteUserCommand(UserId userId) {
}
