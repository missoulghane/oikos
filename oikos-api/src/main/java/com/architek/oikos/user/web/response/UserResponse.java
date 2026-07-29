package com.architek.oikos.user.web.response;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.architek.oikos.user.application.dto.UserView;

public record UserResponse(String id, String fullName, String email, Set<String> roles,
                            Map<String, String> roleByProperty, boolean verified, boolean enabled) {

    public static UserResponse from(UserView view) {
        return new UserResponse(
                view.id().toString(),
                view.fullName(),
                view.email(),
                view.roles().stream().map(Enum::name).collect(Collectors.toSet()),
                view.roleByProperty().entrySet().stream()
                        .collect(Collectors.toMap(Map.Entry::getKey, entry -> entry.getValue().name())),
                view.verified(),
                view.enabled());
    }
}
