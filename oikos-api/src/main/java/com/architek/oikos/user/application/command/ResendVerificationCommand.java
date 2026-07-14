package com.architek.oikos.user.application.command;

import com.architek.oikos.shared.domain.valueobject.EmailVO;

public record ResendVerificationCommand(EmailVO email) {
}
