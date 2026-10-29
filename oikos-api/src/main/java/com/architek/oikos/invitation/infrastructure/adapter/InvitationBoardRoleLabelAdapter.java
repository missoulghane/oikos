package com.architek.oikos.invitation.infrastructure.adapter;

import org.springframework.stereotype.Component;

import com.architek.oikos.invitation.application.port.out.BoardRoleLabelPort;
import com.architek.oikos.property.application.port.in.ListBoardRolesUseCase;

@Component
public class InvitationBoardRoleLabelAdapter implements BoardRoleLabelPort {

    private final ListBoardRolesUseCase listBoardRolesUseCase;

    public InvitationBoardRoleLabelAdapter(ListBoardRolesUseCase listBoardRolesUseCase) {
        this.listBoardRolesUseCase = listBoardRolesUseCase;
    }

    @Override
    public String labelOf(String boardRoleCode) {
        return listBoardRolesUseCase.listBoardRoles().stream()
                .filter(option -> option.getCode().name().equals(boardRoleCode))
                .map(option -> option.getLabel())
                .findFirst()
                .orElse(boardRoleCode);
    }
}
