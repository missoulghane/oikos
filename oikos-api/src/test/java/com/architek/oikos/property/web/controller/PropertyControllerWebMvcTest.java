package com.architek.oikos.property.web.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
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
import com.architek.oikos.property.application.dto.PropertyView;
import com.architek.oikos.property.application.port.in.CreatePropertyUseCase;
import com.architek.oikos.property.application.port.in.GetPropertyUseCase;
import com.architek.oikos.property.application.port.in.ListPropertiesUseCase;
import com.architek.oikos.property.domain.valueobject.PropertyId;
import com.architek.oikos.shared.domain.pagination.Page;
import com.architek.oikos.shared.domain.valueobject.EntityId;
import com.architek.oikos.testsupport.WebSecuritySliceTestConfiguration;

@WebMvcTest(controllers = PropertyController.class)
@Import(WebSecuritySliceTestConfiguration.class)
class PropertyControllerWebMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtService jwtService;

    @MockitoBean
    private CreatePropertyUseCase createPropertyUseCase;

    @MockitoBean
    private GetPropertyUseCase getPropertyUseCase;

    @MockitoBean
    private ListPropertiesUseCase listPropertiesUseCase;

    private String bearerToken(String... authorities) {
        return "Bearer " + jwtService.generateAccessToken(EntityId.of(UUID.randomUUID()), Set.of(authorities));
    }

    @Test
    void anonymous_request_is_rejected_with_401() throws Exception {
        mockMvc.perform(get("/api/v1/properties")).andExpect(status().isUnauthorized());
    }

    @Test
    void regular_user_is_forbidden_from_listing_properties() throws Exception {
        mockMvc.perform(get("/api/v1/properties").header("Authorization", bearerToken("ROLE_USER")))
                .andExpect(status().isForbidden());
    }

    @Test
    void admin_can_list_properties() throws Exception {
        when(listPropertiesUseCase.listProperties(any())).thenReturn(Page.of(List.of(), 0, 20, 0));

        mockMvc.perform(get("/api/v1/properties").header("Authorization", bearerToken("ROLE_ADMIN")))
                .andExpect(status().isOk());
    }

    @Test
    void admin_can_get_a_property_by_id() throws Exception {
        PropertyId id = PropertyId.newId();
        when(getPropertyUseCase.getProperty(any())).thenReturn(new PropertyView(id, "Copro", "Address"));

        mockMvc.perform(get("/api/v1/properties/" + id).header("Authorization", bearerToken("ROLE_ADMIN")))
                .andExpect(status().isOk());
    }

    @Test
    void admin_can_create_a_property_with_its_first_building() throws Exception {
        PropertyId id = PropertyId.newId();
        when(createPropertyUseCase.create(any())).thenReturn(id);

        mockMvc.perform(post("/api/v1/properties")
                        .header("Authorization", bearerToken("ROLE_ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Copro Laumiere","address":"33 Avenue de Laumiere","firstBuildingName":"Batiment A","firstBuildingFloorCount":5}
                                """))
                .andExpect(status().isCreated());
    }

    @Test
    void regular_user_is_forbidden_from_creating_a_property() throws Exception {
        mockMvc.perform(post("/api/v1/properties")
                        .header("Authorization", bearerToken("ROLE_USER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Copro Laumiere","address":"33 Avenue de Laumiere","firstBuildingName":"Batiment A","firstBuildingFloorCount":5}
                                """))
                .andExpect(status().isForbidden());
    }

    @Test
    void create_without_first_building_returns_400() throws Exception {
        mockMvc.perform(post("/api/v1/properties")
                        .header("Authorization", bearerToken("ROLE_ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Copro Laumiere","address":"33 Avenue de Laumiere"}
                                """))
                .andExpect(status().isBadRequest());
    }
}
