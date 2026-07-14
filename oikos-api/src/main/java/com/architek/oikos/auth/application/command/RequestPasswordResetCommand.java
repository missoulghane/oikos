package com.architek.oikos.auth.application.command;

import com.architek.oikos.shared.domain.valueobject.EmailVO;

public record RequestPasswordResetCommand(EmailVO email) {
}
