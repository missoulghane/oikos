package com.architek.oikos.user.application.command;

import com.architek.oikos.shared.domain.valueobject.RawPassword;

public record ActivateAccountCommand(String token, RawPassword newPassword) {
}
