package com.architek.oikos.user.web.response;

import java.util.Set;

import com.architek.oikos.user.application.dto.UserView;

public record UserResponse(String id, String login, String fullName, String email, String phone,
                            Set<String> roles, boolean verified, boolean enabled) {

    public static UserResponse from(UserView view) {
        return new UserResponse(
                view.id().toString(),
                view.login(),
                view.fullName(),
                view.email(),
                view.phone(),
                view.roles().stream().map(Enum::name).collect(java.util.stream.Collectors.toSet()),
                view.verified(),
                view.enabled());
    }
}
