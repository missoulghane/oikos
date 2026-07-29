package com.architek.oikos.user.application.command;

import com.architek.oikos.shared.domain.valueobject.EmailVO;
import com.architek.oikos.user.domain.valueobject.UserId;

public record UpdateUserProfileCommand(UserId userId, String fullName, EmailVO email) {
}
