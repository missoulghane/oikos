package com.architek.oikos.property.web.controller;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.architek.oikos.property.application.port.in.ListBoardRolesUseCase;
import com.architek.oikos.property.application.port.in.ListUnitTypeChoicesUseCase;
import com.architek.oikos.property.domain.model.BoardRoleOption;
import com.architek.oikos.property.domain.model.UnitTypeChoice;
import com.architek.oikos.property.domain.valueobject.BoardRole;
import com.architek.oikos.testsupport.WebSecuritySliceTestConfiguration;

/**
 * Runs through the real SecurityConfiguration: both catalogs must stay readable
 * anonymously, since the sign-up wizard shows them before any account exists.
 */
@WebMvcTest(controllers = ReferenceCatalogController.class)
@Import(WebSecuritySliceTestConfiguration.class)
class ReferenceCatalogControllerWebMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ListBoardRolesUseCase listBoardRolesUseCase;

    @MockitoBean
    private ListUnitTypeChoicesUseCase listUnitTypeChoicesUseCase;

    @Test
    void the_board_roles_are_readable_without_authenticating() throws Exception {
        when(listBoardRolesUseCase.listBoardRoles()).thenReturn(List.of(
                BoardRoleOption.of(BoardRole.VOLUNTEER_MANAGER, "Syndic bénévole", 1, true, true),
                BoardRoleOption.of(BoardRole.PROPERTY_MANAGER, "Gestionnaire", 6, false, true)));

        mockMvc.perform(get("/api/v1/board-roles"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].code").value("VOLUNTEER_MANAGER"))
                .andExpect(jsonPath("$[0].label").value("Syndic bénévole"))
                .andExpect(jsonPath("$[0].availableAtSignup").value(true))
                .andExpect(jsonPath("$[1].availableAtSignup").value(false));
    }

    @Test
    void the_unit_type_choices_are_readable_without_authenticating() throws Exception {
        when(listUnitTypeChoicesUseCase.listUnitTypeChoices()).thenReturn(List.of(
                UnitTypeChoice.of("Parking", "parkings", "Places de stationnement.", 2, true)));

        mockMvc.perform(get("/api/v1/unit-type-choices"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Parking"))
                .andExpect(jsonPath("$[0].plural").value("parkings"))
                .andExpect(jsonPath("$[0].description").value("Places de stationnement."));
    }
}
