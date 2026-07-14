package com.architek.oikos.user.application.command;

import com.architek.oikos.shared.domain.valueobject.RawPassword;
import com.architek.oikos.user.domain.valueobject.UserId;

public record ChangePasswordCommand(UserId userId, RawPassword currentPassword, RawPassword newPassword) {
}
