package com.architek.oikos.property.web.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
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
import com.architek.oikos.property.application.dto.BuildingView;
import com.architek.oikos.property.application.port.in.AddBuildingUseCase;
import com.architek.oikos.property.application.port.in.GetBuildingUseCase;
import com.architek.oikos.property.application.port.in.ListBuildingsByPropertyUseCase;
import com.architek.oikos.property.application.port.in.UpdateBuildingUseCase;
import com.architek.oikos.property.domain.valueobject.PropertyId;
import com.architek.oikos.property.domain.valueobject.BuildingId;
import com.architek.oikos.shared.domain.pagination.Page;
import com.architek.oikos.shared.domain.valueobject.EntityId;
import com.architek.oikos.testsupport.WebSecuritySliceTestConfiguration;

@WebMvcTest(controllers = BuildingController.class)
@Import(WebSecuritySliceTestConfiguration.class)
class BuildingControllerWebMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtService jwtService;

    @MockitoBean
    private AddBuildingUseCase addBuildingUseCase;

    @MockitoBean
    private GetBuildingUseCase getBuildingUseCase;

    @MockitoBean
    private ListBuildingsByPropertyUseCase listBuildingsByPropertyUseCase;

    @MockitoBean
    private UpdateBuildingUseCase updateBuildingUseCase;

    private String bearerToken(String... authorities) {
        return "Bearer " + jwtService.generateAccessToken(EntityId.of(UUID.randomUUID()), Set.of(authorities));
    }

    @Test
    void anonymous_request_is_rejected_with_401() throws Exception {
        mockMvc.perform(get("/api/v1/properties/" + PropertyId.newId() + "/buildings"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void admin_can_list_buildings_of_a_property() throws Exception {
        when(listBuildingsByPropertyUseCase.listBuildings(any())).thenReturn(Page.of(List.of(), 0, 20, 0));

        mockMvc.perform(get("/api/v1/properties/" + PropertyId.newId() + "/buildings")
                        .header("Authorization", bearerToken("ROLE_ADMIN")))
                .andExpect(status().isOk());
    }

    @Test
    void admin_can_get_an_building_by_id() throws Exception {
        PropertyId propertyId = PropertyId.newId();
        BuildingId id = BuildingId.newId();
        when(getBuildingUseCase.getBuilding(any())).thenReturn(new BuildingView(id, propertyId, "Batiment A", 5));

        mockMvc.perform(get("/api/v1/buildings/" + id).header("Authorization", bearerToken("ROLE_ADMIN")))
                .andExpect(status().isOk());
    }

    @Test
    void admin_can_add_an_building_to_a_property() throws Exception {
        BuildingId id = BuildingId.newId();
        when(addBuildingUseCase.add(any())).thenReturn(id);

        mockMvc.perform(post("/api/v1/properties/" + PropertyId.newId() + "/buildings")
                        .header("Authorization", bearerToken("ROLE_ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Batiment B","floorCount":3}
                                """))
                .andExpect(status().isCreated());
    }

    @Test
    void regular_user_is_forbidden_from_adding_an_building() throws Exception {
        mockMvc.perform(post("/api/v1/properties/" + PropertyId.newId() + "/buildings")
                        .header("Authorization", bearerToken("ROLE_USER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Batiment B","floorCount":3}
                                """))
                .andExpect(status().isForbidden());
    }

    @Test
    void admin_can_rename_a_building_and_fix_its_floor_count() throws Exception {
        PropertyId propertyId = PropertyId.newId();
        BuildingId id = BuildingId.newId();
        when(updateBuildingUseCase.update(any())).thenReturn(new BuildingView(id, propertyId, "Bâtiment B", 7));

        mockMvc.perform(put("/api/v1/buildings/" + id)
                        .header("Authorization", bearerToken("ROLE_ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Bâtiment B\",\"floorCount\":7}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Bâtiment B"))
                .andExpect(jsonPath("$.floorCount").value(7));
    }

    @Test
    void a_building_cannot_be_left_without_a_name() throws Exception {
        mockMvc.perform(put("/api/v1/buildings/" + BuildingId.newId())
                        .header("Authorization", bearerToken("ROLE_ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"  \",\"floorCount\":7}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void a_negative_floor_count_is_rejected() throws Exception {
        mockMvc.perform(put("/api/v1/buildings/" + BuildingId.newId())
                        .header("Authorization", bearerToken("ROLE_ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Bâtiment B\",\"floorCount\":-1}"))
                .andExpect(status().isBadRequest());
    }
}
