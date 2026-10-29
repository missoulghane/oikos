package com.architek.oikos.property.application.port.in;

import java.util.List;

import com.architek.oikos.property.domain.model.BoardRoleOption;

/** The board seats on offer, for every screen that picks one (sign-up, "Ajouter un membre"). */
public interface ListBoardRolesUseCase {

    List<BoardRoleOption> listBoardRoles();
}
