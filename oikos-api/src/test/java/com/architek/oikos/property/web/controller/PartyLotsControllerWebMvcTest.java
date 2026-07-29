package com.architek.oikos.property.web.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.architek.oikos.auth.infrastructure.security.JwtService;
import com.architek.oikos.party.application.dto.PartyView;
import com.architek.oikos.party.application.port.in.GetPartyUseCase;
import com.architek.oikos.party.domain.valueobject.PartyId;
import com.architek.oikos.property.application.port.in.ListLotsByPartyUseCase;
import com.architek.oikos.shared.domain.valueobject.EntityId;
import com.architek.oikos.shared.domain.valueobject.PartyType;
import com.architek.oikos.testsupport.WebSecuritySliceTestConfiguration;
import com.architek.oikos.user.application.dto.UserAccessView;
import com.architek.oikos.user.application.port.in.GetUserAccessUseCase;

@WebMvcTest(controllers = PartyLotsController.class)
@Import(WebSecuritySliceTestConfiguration.class)
class PartyLotsControllerWebMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private GetUserAccessUseCase getUserAccessUseCase;

    @MockitoBean
    private ListLotsByPartyUseCase listLotsByPartyUseCase;

    @MockitoBean
    private GetPartyUseCase getPartyUseCase;

    private String bearerToken(String... authorities) {
        return "Bearer " + jwtService.generateAccessToken(EntityId.of(UUID.randomUUID()), Set.of(authorities));
    }

    @Test
    void anonymous_request_is_rejected_with_401() throws Exception {
        mockMvc.perform(get("/api/v1/parties/" + EntityId.newId() + "/lots")).andExpect(status().isUnauthorized());
    }

    @Test
    void admin_can_list_lots_of_a_party() throws Exception {
        when(listLotsByPartyUseCase.listLots(any())).thenReturn(List.of());

        mockMvc.perform(get("/api/v1/parties/" + EntityId.newId() + "/lots")
                        .header("Authorization", bearerToken("ROLE_ADMIN")))
                .andExpect(status().isOk());
    }

    @Test
    void property_manager_can_list_lots_of_a_party_in_their_managed_property() throws Exception {
        EntityId partyId = EntityId.newId();
        String propertyId = UUID.randomUUID().toString();
        when(getPartyUseCase.getParty(any())).thenReturn(
                new PartyView(PartyId.of(partyId.toString()),
                        EntityId.of(propertyId), "Jane Doe", PartyType.INDIVIDUAL, "jane@doe.com", null));
        when(getUserAccessUseCase.getAccess(any()))
                .thenReturn(new UserAccessView(Set.of(), Set.of(propertyId), Set.of()));
        when(listLotsByPartyUseCase.listLots(any())).thenReturn(List.of());

        mockMvc.perform(get("/api/v1/parties/" + partyId + "/lots")
                        .header("Authorization", bearerToken("ROLE_PROPERTY_MANAGER")))
                .andExpect(status().isOk());
    }

    @Test
    void owner_can_list_lots_of_their_own_party() throws Exception {
        EntityId partyId = EntityId.newId();
        when(getPartyUseCase.getParty(any())).thenReturn(
                new PartyView(PartyId.of(partyId.toString()), EntityId.newId(), "Jane Doe", PartyType.INDIVIDUAL,
                        "jane@doe.com", null));
        when(getUserAccessUseCase.getAccess(any()))
                .thenReturn(new UserAccessView(Set.of(), Set.of(), Set.of(partyId.toString())));
        when(listLotsByPartyUseCase.listLots(any())).thenReturn(List.of());

        mockMvc.perform(get("/api/v1/parties/" + partyId + "/lots")
                        .header("Authorization", bearerToken("ROLE_USER")))
                .andExpect(status().isOk());
    }

    @Test
    void regular_user_is_forbidden_from_listing_lots_of_someone_elses_party() throws Exception {
        EntityId partyId = EntityId.newId();
        when(getPartyUseCase.getParty(any())).thenReturn(
                new PartyView(PartyId.of(partyId.toString()), EntityId.newId(), "Jane Doe", PartyType.INDIVIDUAL,
                        "jane@doe.com", null));

        mockMvc.perform(get("/api/v1/parties/" + partyId + "/lots")
                        .header("Authorization", bearerToken("ROLE_USER")))
                .andExpect(status().isForbidden());
    }
}
