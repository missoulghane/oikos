package com.architek.oikos.property.web.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.architek.oikos.auth.infrastructure.security.JwtService;
import com.architek.oikos.property.application.dto.BuildingView;
import com.architek.oikos.property.application.dto.UnitView;
import com.architek.oikos.property.application.port.in.CountUnitsByPropertyUseCase;
import com.architek.oikos.property.application.port.in.AddUnitUseCase;
import com.architek.oikos.property.application.port.in.GetBuildingUseCase;
import com.architek.oikos.property.application.port.in.GetUnitUseCase;
import com.architek.oikos.property.application.port.in.ListUnitsByBuildingUseCase;
import com.architek.oikos.property.application.query.ListUnitsByBuildingQuery;
import com.architek.oikos.property.application.port.in.UpdateUnitSharesUseCase;
import com.architek.oikos.property.domain.valueobject.BuildingId;
import com.architek.oikos.property.domain.valueobject.PropertyId;
import com.architek.oikos.property.domain.valueobject.UnitId;
import com.architek.oikos.property.domain.valueobject.OwnershipStatus;
import com.architek.oikos.property.domain.valueobject.UnitTypeDefinitionId;
import com.architek.oikos.shared.domain.pagination.Page;
import com.architek.oikos.shared.domain.valueobject.EntityId;
import com.architek.oikos.testsupport.WebSecuritySliceTestConfiguration;

@WebMvcTest(controllers = UnitController.class)
@Import(WebSecuritySliceTestConfiguration.class)
class UnitControllerWebMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtService jwtService;

    @MockitoBean
    private AddUnitUseCase addUnitUseCase;

    @MockitoBean
    private CountUnitsByPropertyUseCase countUnitsByPropertyUseCase;

    @MockitoBean
    private GetUnitUseCase getUnitUseCase;

    @MockitoBean
    private ListUnitsByBuildingUseCase listUnitsByBuildingUseCase;

    @MockitoBean
    private GetBuildingUseCase getBuildingUseCase;

    @MockitoBean
    private UpdateUnitSharesUseCase updateUnitSharesUseCase;

    private String bearerToken(String... authorities) {
        return "Bearer " + jwtService.generateAccessToken(EntityId.of(UUID.randomUUID()), Set.of(authorities));
    }

    @Test
    void anonymous_request_is_rejected_with_401() throws Exception {
        mockMvc.perform(get("/api/v1/buildings/" + BuildingId.newId() + "/units"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void admin_can_list_units_of_an_building() throws Exception {
        when(listUnitsByBuildingUseCase.listUnits(any())).thenReturn(Page.of(List.of(), 0, 20, 0));

        mockMvc.perform(get("/api/v1/buildings/" + BuildingId.newId() + "/units")
                        .header("Authorization", bearerToken("ROLE_ADMIN")))
                .andExpect(status().isOk());
    }

    @Test
    void admin_can_get_a_unit_by_id() throws Exception {
        BuildingId buildingId = BuildingId.newId();
        UnitId id = UnitId.newId();
        when(getUnitUseCase.getUnit(any())).thenReturn(
                new UnitView(id, buildingId, PropertyId.newId(), "A12", UnitTypeDefinitionId.newId(), "Appartement", BigDecimal.TEN,
                        OwnershipStatus.NOT_AFFECTED, List.of(), null));

        mockMvc.perform(get("/api/v1/units/" + id).header("Authorization", bearerToken("ROLE_ADMIN")))
                .andExpect(status().isOk());
    }

    @Test
    void admin_can_add_a_unit_to_an_building() throws Exception {
        UnitId id = UnitId.newId();
        when(addUnitUseCase.add(any())).thenReturn(id);

        mockMvc.perform(post("/api/v1/buildings/" + BuildingId.newId() + "/units")
                        .header("Authorization", bearerToken("ROLE_ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"unitNumber":"A12","unitTypeId":"%s","shares":150}
                                """.formatted(UnitTypeDefinitionId.newId())))
                .andExpect(status().isCreated());
    }

    @Test
    void admin_can_update_a_unit_s_shares() throws Exception {
        UnitId id = UnitId.newId();
        when(updateUnitSharesUseCase.updateShares(any())).thenReturn(
                new UnitView(id, BuildingId.newId(), PropertyId.newId(), "A12", UnitTypeDefinitionId.newId(), "Appartement",
                        new BigDecimal("150"), OwnershipStatus.NOT_AFFECTED, List.of(), null));

        mockMvc.perform(put("/api/v1/units/" + id + "/shares")
                        .header("Authorization", bearerToken("ROLE_ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"shares":150}
                                """))
                .andExpect(status().isOk());
    }

    @Test
    void regular_user_is_forbidden_from_adding_a_unit() throws Exception {
        BuildingId buildingId = BuildingId.newId();
        when(getBuildingUseCase.getBuilding(any())).thenReturn(
                new BuildingView(buildingId, PropertyId.newId(), "A", 3));

        mockMvc.perform(post("/api/v1/buildings/" + buildingId + "/units")
                        .header("Authorization", bearerToken("ROLE_USER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"unitNumber":"A12","unitTypeId":"%s","shares":150}
                                """.formatted(UnitTypeDefinitionId.newId())))
                .andExpect(status().isForbidden());
    }

    @Test
    void the_search_and_ownership_status_filters_are_bound_from_the_query_string() throws Exception {
        when(listUnitsByBuildingUseCase.listUnits(any())).thenReturn(Page.of(List.of(), 0, 20, 0));

        mockMvc.perform(get("/api/v1/buildings/" + BuildingId.newId() + "/units")
                        .param("search", "A12")
                        .param("ownershipStatus", "NOT_AFFECTED")
                        .header("Authorization", bearerToken("ROLE_ADMIN")))
                .andExpect(status().isOk());

        var captor = ArgumentCaptor.forClass(ListUnitsByBuildingQuery.class);
        verify(listUnitsByBuildingUseCase).listUnits(captor.capture());
        assertThat(captor.getValue().search()).isEqualTo("A12");
        assertThat(captor.getValue().ownershipStatus()).isEqualTo(OwnershipStatus.NOT_AFFECTED);
    }
}
