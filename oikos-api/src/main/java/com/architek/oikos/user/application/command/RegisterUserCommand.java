package com.architek.oikos.user.application.command;

import com.architek.oikos.shared.domain.valueobject.EmailVO;
import com.architek.oikos.shared.domain.valueobject.RawPassword;
import com.architek.oikos.user.domain.model.Role;

public record RegisterUserCommand(String fullName, EmailVO email, RawPassword password, Role role) {
}
