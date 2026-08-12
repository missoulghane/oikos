package com.architek.oikos.user.application.command;

import com.architek.oikos.user.domain.valueobject.UserId;

public record UpdateAvatarCommand(UserId userId, byte[] content, String contentType) {
}
