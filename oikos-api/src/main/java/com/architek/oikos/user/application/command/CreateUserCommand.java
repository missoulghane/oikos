package com.architek.oikos.user.application.command;

import com.architek.oikos.shared.domain.valueobject.EmailVO;

public record CreateUserCommand(String lastName, String firstName, EmailVO email, String phone, String login) {
}
