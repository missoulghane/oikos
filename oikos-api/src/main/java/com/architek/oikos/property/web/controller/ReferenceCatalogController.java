package com.architek.oikos.property.web.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.architek.oikos.property.application.port.in.ListBoardRolesUseCase;
import com.architek.oikos.property.application.port.in.ListUnitTypeChoicesUseCase;
import com.architek.oikos.property.web.response.BoardRoleResponse;
import com.architek.oikos.property.web.response.UnitTypeChoiceResponse;

/**
 * The property catalogs the clients pick from, read-only. Public (see
 * SecurityConfiguration): the sign-up wizard shows both before any account
 * exists. They are the product's own lists - no copropriété's data, nobody
 * named - so there is nothing to guard.
 */
@RestController
public class ReferenceCatalogController {

    private final ListBoardRolesUseCase listBoardRolesUseCase;
    private final ListUnitTypeChoicesUseCase listUnitTypeChoicesUseCase;

    public ReferenceCatalogController(ListBoardRolesUseCase listBoardRolesUseCase,
                                      ListUnitTypeChoicesUseCase listUnitTypeChoicesUseCase) {
        this.listBoardRolesUseCase = listBoardRolesUseCase;
        this.listUnitTypeChoicesUseCase = listUnitTypeChoicesUseCase;
    }

    @GetMapping("/board-roles")
    public List<BoardRoleResponse> boardRoles() {
        return listBoardRolesUseCase.listBoardRoles().stream().map(BoardRoleResponse::from).toList();
    }

    @GetMapping("/unit-type-choices")
    public List<UnitTypeChoiceResponse> unitTypeChoices() {
        return listUnitTypeChoicesUseCase.listUnitTypeChoices().stream().map(UnitTypeChoiceResponse::from).toList();
    }
}
