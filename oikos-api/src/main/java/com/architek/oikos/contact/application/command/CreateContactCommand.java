package com.architek.oikos.contact.application.command;

import com.architek.oikos.shared.domain.valueobject.EmailVO;

public record CreateContactCommand(String lastName, String firstName, EmailVO email, String phone) {
}
