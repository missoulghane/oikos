package com.architek.oikos.user.domain.exception;

import com.architek.oikos.shared.exception.BusinessException;
import com.architek.oikos.user.domain.model.Role;

public class RoleNotAllowedException extends BusinessException {

    public RoleNotAllowedException(Role role) {
        super("Role not allowed for self-registration: " + role);
    }
}
