package com.architek.oikos.party.web.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
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
import com.architek.oikos.party.application.dto.PartyView;
import com.architek.oikos.party.application.port.in.CreatePartyUseCase;
import com.architek.oikos.party.application.port.in.DeletePartyUseCase;
import com.architek.oikos.party.application.port.in.GetPartyUseCase;
import com.architek.oikos.party.application.port.in.ListPartiesUseCase;
import com.architek.oikos.party.application.port.in.UpdatePartyUseCase;
import com.architek.oikos.party.domain.valueobject.PartyId;
import com.architek.oikos.shared.domain.valueobject.PartyType;
import com.architek.oikos.shared.domain.pagination.Page;
import com.architek.oikos.shared.domain.valueobject.EntityId;
import com.architek.oikos.testsupport.WebSecuritySliceTestConfiguration;

@WebMvcTest(controllers = PartyController.class)
@Import(WebSecuritySliceTestConfiguration.class)
class PartyControllerWebMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtService jwtService;

    @MockitoBean
    private CreatePartyUseCase createPartyUseCase;

    @MockitoBean
    private GetPartyUseCase getPartyUseCase;

    @MockitoBean
    private UpdatePartyUseCase updatePartyUseCase;

    @MockitoBean
    private ListPartiesUseCase listPartiesUseCase;

    @MockitoBean
    private DeletePartyUseCase deletePartyUseCase;

    private String bearerToken(String... authorities) {
        return "Bearer " + jwtService.generateAccessToken(EntityId.of(UUID.randomUUID()), Set.of(authorities));
    }

    @Test
    void anonymous_request_is_rejected_with_401() throws Exception {
        mockMvc.perform(get("/api/v1/parties")).andExpect(status().isUnauthorized());
    }

    @Test
    void regular_user_is_forbidden_from_listing_parties() throws Exception {
        mockMvc.perform(get("/api/v1/parties").header("Authorization", bearerToken("ROLE_USER")))
                .andExpect(status().isForbidden());
    }

    @Test
    void admin_can_list_parties() throws Exception {
        when(listPartiesUseCase.listParties(any())).thenReturn(Page.of(List.of(), 0, 20, 0));

        mockMvc.perform(get("/api/v1/parties").header("Authorization", bearerToken("ROLE_ADMIN")))
                .andExpect(status().isOk());
    }

    @Test
    void property_manager_can_list_parties() throws Exception {
        when(listPartiesUseCase.listParties(any())).thenReturn(Page.of(List.of(), 0, 20, 0));

        mockMvc.perform(get("/api/v1/parties").header("Authorization", bearerToken("ROLE_PROPERTY_MANAGER")))
                .andExpect(status().isOk());
    }

    @Test
    void property_manager_is_forbidden_from_creating_a_party() throws Exception {
        mockMvc.perform(post("/api/v1/parties")
                        .header("Authorization", bearerToken("ROLE_PROPERTY_MANAGER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"fullName":"Jane Doe","partyType":"INDIVIDUAL","email":"jane@doe.com"}
                                """))
                .andExpect(status().isForbidden());
    }

    @Test
    void admin_can_get_a_party_by_id() throws Exception {
        PartyId id = PartyId.newId();
        when(getPartyUseCase.getParty(any())).thenReturn(new PartyView(id, "Jane Doe", PartyType.INDIVIDUAL, "jane@doe.com", null));

        mockMvc.perform(get("/api/v1/parties/" + id).header("Authorization", bearerToken("ROLE_ADMIN")))
                .andExpect(status().isOk());
    }

    @Test
    void admin_can_create_a_party_returns_201_with_location_header() throws Exception {
        PartyId id = PartyId.newId();
        when(createPartyUseCase.create(any())).thenReturn(id);

        mockMvc.perform(post("/api/v1/parties")
                        .header("Authorization", bearerToken("ROLE_ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"fullName":"Jane Doe","partyType":"INDIVIDUAL","email":"jane@doe.com"}
                                """))
                .andExpect(status().isCreated());
    }

    @Test
    void regular_user_is_forbidden_from_creating_a_party() throws Exception {
        mockMvc.perform(post("/api/v1/parties")
                        .header("Authorization", bearerToken("ROLE_USER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"fullName":"Jane Doe","partyType":"INDIVIDUAL","email":"jane@doe.com"}
                                """))
                .andExpect(status().isForbidden());
    }

    @Test
    void admin_can_update_a_party() throws Exception {
        PartyId id = PartyId.newId();
        when(updatePartyUseCase.update(any())).thenReturn(new PartyView(id, "Janet Smith", PartyType.COMPANY, "janet@smith.com", null));

        mockMvc.perform(put("/api/v1/parties/" + id)
                        .header("Authorization", bearerToken("ROLE_ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"fullName":"Janet Smith","partyType":"COMPANY","email":"janet@smith.com"}
                                """))
                .andExpect(status().isOk());
    }

    @Test
    void admin_can_delete_a_party() throws Exception {
        mockMvc.perform(delete("/api/v1/parties/" + PartyId.newId())
                        .header("Authorization", bearerToken("ROLE_ADMIN")))
                .andExpect(status().isNoContent());
    }

    @Test
    void regular_user_is_forbidden_from_deleting_a_party() throws Exception {
        mockMvc.perform(delete("/api/v1/parties/" + PartyId.newId())
                        .header("Authorization", bearerToken("ROLE_USER")))
                .andExpect(status().isForbidden());
    }
}
