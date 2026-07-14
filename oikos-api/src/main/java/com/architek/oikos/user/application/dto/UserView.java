package com.architek.oikos.user.application.dto;

import java.util.Set;

import com.architek.oikos.user.application.port.out.ContactDetails;
import com.architek.oikos.user.domain.model.Role;
import com.architek.oikos.user.domain.model.User;
import com.architek.oikos.user.domain.valueobject.UserId;

public record UserView(UserId id, String login, String lastName, String firstName, String email, String phone,
                        Set<Role> roles, boolean verified, boolean enabled) {

    public static UserView of(User user, ContactDetails contact) {
        return new UserView(user.getId(), user.getLogin(), contact.lastName(), contact.firstName(),
                contact.email().value(), contact.phone(), user.getRoles(), user.isVerified(), user.isEnabled());
    }
}
