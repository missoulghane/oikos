package com.architek.oikos.user.application.dto;

import java.util.Set;

import com.architek.oikos.user.application.port.out.PartyDetails;
import com.architek.oikos.user.domain.model.Role;
import com.architek.oikos.user.domain.model.User;
import com.architek.oikos.user.domain.valueobject.UserId;

public record UserView(UserId id, String login, String fullName, String email, String phone,
                        Set<Role> roles, boolean verified, boolean enabled) {

    public static UserView of(User user, PartyDetails party) {
        return new UserView(user.getId(), user.getLogin(), party.fullName(),
                party.email().value(), party.phone(), user.getRoles(), user.isVerified(), user.isEnabled());
    }
}
