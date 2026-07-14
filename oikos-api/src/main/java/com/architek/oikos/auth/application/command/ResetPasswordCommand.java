package com.architek.oikos.auth.application.command;

import com.architek.oikos.shared.domain.valueobject.RawPassword;

public record ResetPasswordCommand(String token, RawPassword newPassword) {
}
