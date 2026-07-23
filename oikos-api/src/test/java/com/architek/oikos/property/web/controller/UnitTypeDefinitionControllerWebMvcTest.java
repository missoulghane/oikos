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
import com.architek.oikos.property.application.port.in.AddUnitTypeDefinitionUseCase;
import com.architek.oikos.property.application.port.in.ListUnitTypeDefinitionsByPropertyUseCase;
import com.architek.oikos.property.application.port.in.RemoveUnitTypeDefinitionUseCase;
import com.architek.oikos.property.domain.valueobject.PropertyId;
import com.architek.oikos.property.domain.valueobject.UnitTypeDefinitionId;
import com.architek.oikos.shared.domain.valueobject.EntityId;
import com.architek.oikos.testsupport.WebSecuritySliceTestConfiguration;

@WebMvcTest(controllers = UnitTypeDefinitionController.class)
@Import(WebSecuritySliceTestConfiguration.class)
class UnitTypeDefinitionControllerWebMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtService jwtService;

    @MockitoBean
    private AddUnitTypeDefinitionUseCase addUnitTypeDefinitionUseCase;

    @MockitoBean
    private ListUnitTypeDefinitionsByPropertyUseCase listUnitTypeDefinitionsByPropertyUseCase;

    @MockitoBean
    private RemoveUnitTypeDefinitionUseCase removeUnitTypeDefinitionUseCase;

    private String bearerToken(String... authorities) {
        return "Bearer " + jwtService.generateAccessToken(EntityId.of(UUID.randomUUID()), Set.of(authorities));
    }

    @Test
    void anonymous_request_is_rejected_with_401() throws Exception {
        mockMvc.perform(get("/api/v1/properties/" + PropertyId.newId() + "/unit-types"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void admin_can_list_unit_types_of_a_property() throws Exception {
        when(listUnitTypeDefinitionsByPropertyUseCase.listUnitTypeDefinitions(any())).thenReturn(List.of());

        mockMvc.perform(get("/api/v1/properties/" + PropertyId.newId() + "/unit-types")
                        .header("Authorization", bearerToken("ROLE_ADMIN")))
                .andExpect(status().isOk());
    }

    @Test
    void admin_can_add_a_unit_type() throws Exception {
        when(addUnitTypeDefinitionUseCase.add(any())).thenReturn(UnitTypeDefinitionId.newId());

        mockMvc.perform(post("/api/v1/properties/" + PropertyId.newId() + "/unit-types")
                        .header("Authorization", bearerToken("ROLE_ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Duplex"}
                                """))
                .andExpect(status().isCreated());
    }

    @Test
    void admin_can_remove_a_unit_type() throws Exception {
        mockMvc.perform(delete("/api/v1/properties/" + PropertyId.newId() + "/unit-types/" + UnitTypeDefinitionId.newId())
                        .header("Authorization", bearerToken("ROLE_ADMIN")))
                .andExpect(status().isNoContent());
    }
}
