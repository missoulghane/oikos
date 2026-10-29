package com.architek.oikos.property.web.response;

import com.architek.oikos.property.domain.model.BoardRoleOption;

/** A board seat catalog row as the clients read it. */
public record BoardRoleResponse(String code, String label, int position, boolean availableAtSignup) {

    public static BoardRoleResponse from(BoardRoleOption option) {
        return new BoardRoleResponse(option.getCode().name(), option.getLabel(), option.getPosition(),
                option.isAvailableAtSignup());
    }
}
