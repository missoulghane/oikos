package com.architek.oikos.property.web.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.architek.oikos.auth.infrastructure.security.JwtService;
import com.architek.oikos.property.application.port.in.AddUnitOwnerUseCase;
import com.architek.oikos.property.application.port.in.AddUnitOwnershipUseCase;
import com.architek.oikos.property.application.port.in.ListUnitOwnershipsByUnitUseCase;
import com.architek.oikos.property.application.port.in.RemoveUnitOwnershipUseCase;
import com.architek.oikos.property.domain.valueobject.UnitId;
import com.architek.oikos.property.domain.valueobject.UnitOwnershipId;
import com.architek.oikos.shared.domain.valueobject.EntityId;
import com.architek.oikos.testsupport.WebSecuritySliceTestConfiguration;

@WebMvcTest(controllers = UnitOwnershipController.class)
@Import(WebSecuritySliceTestConfiguration.class)
class UnitOwnershipControllerWebMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtService jwtService;

    @MockitoBean
    private AddUnitOwnershipUseCase addUnitOwnershipUseCase;

    @MockitoBean
    private AddUnitOwnerUseCase addUnitOwnerUseCase;

    @MockitoBean
    private ListUnitOwnershipsByUnitUseCase listUnitOwnershipsByUnitUseCase;

    @MockitoBean
    private RemoveUnitOwnershipUseCase removeUnitOwnershipUseCase;

    private String bearerToken(String... authorities) {
        return "Bearer " + jwtService.generateAccessToken(EntityId.of(UUID.randomUUID()), Set.of(authorities));
    }

    @Test
    void anonymous_request_is_rejected_with_401() throws Exception {
        mockMvc.perform(get("/api/v1/units/" + UnitId.newId() + "/owners")).andExpect(status().isUnauthorized());
    }

    @Test
    void admin_can_list_owners_of_a_unit() throws Exception {
        when(listUnitOwnershipsByUnitUseCase.listUnitOwnerships(any())).thenReturn(List.of());

        mockMvc.perform(get("/api/v1/units/" + UnitId.newId() + "/owners")
                        .header("Authorization", bearerToken("ROLE_ADMIN")))
                .andExpect(status().isOk());
    }

    @Test
    void admin_can_add_an_owner_to_a_unit() throws Exception {
        when(addUnitOwnershipUseCase.add(any())).thenReturn(UnitOwnershipId.newId());

        mockMvc.perform(post("/api/v1/units/" + UnitId.newId() + "/owners")
                        .header("Authorization", bearerToken("ROLE_ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"partyId":"%s","ownershipShare":50}
                                """.formatted(EntityId.newId())))
                .andExpect(status().isCreated());
    }

    @Test
    void regular_user_is_forbidden_from_adding_an_owner() throws Exception {
        mockMvc.perform(post("/api/v1/units/" + UnitId.newId() + "/owners")
                        .header("Authorization", bearerToken("ROLE_USER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"partyId":"%s","ownershipShare":50}
                                """.formatted(EntityId.newId())))
                .andExpect(status().isForbidden());
    }

    @Test
    void ownership_share_above_100_returns_400() throws Exception {
        mockMvc.perform(post("/api/v1/units/" + UnitId.newId() + "/owners")
                        .header("Authorization", bearerToken("ROLE_ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"partyId":"%s","ownershipShare":150}
                                """.formatted(EntityId.newId())))
                .andExpect(status().isBadRequest());
    }

    @Test
    void admin_can_remove_an_owner() throws Exception {
        mockMvc.perform(delete("/api/v1/units/" + UnitId.newId() + "/owners/" + UnitOwnershipId.newId())
                        .header("Authorization", bearerToken("ROLE_ADMIN")))
                .andExpect(status().isNoContent());
    }

    @Test
    void admin_can_add_an_owner_by_creating_its_party() throws Exception {
        when(addUnitOwnerUseCase.add(any())).thenReturn(UnitOwnershipId.newId());

        mockMvc.perform(post("/api/v1/units/" + UnitId.newId() + "/owners/new-party")
                        .header("Authorization", bearerToken("ROLE_ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"fullName":"Jane Doe","partyType":"INDIVIDUAL","email":"jane.doe@example.com","ownershipShare":50}
                                """))
                .andExpect(status().isCreated());
    }

    @Test
    void adding_an_owner_by_party_with_an_invalid_email_returns_400() throws Exception {
        mockMvc.perform(post("/api/v1/units/" + UnitId.newId() + "/owners/new-party")
                        .header("Authorization", bearerToken("ROLE_ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"fullName":"Jane Doe","partyType":"INDIVIDUAL","email":"not-an-email","ownershipShare":50}
                                """))
                .andExpect(status().isBadRequest());
    }
}
