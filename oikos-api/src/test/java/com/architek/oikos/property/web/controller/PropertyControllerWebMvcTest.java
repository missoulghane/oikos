package com.architek.oikos.property.web.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
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
import com.architek.oikos.property.application.dto.PropertyView;
import com.architek.oikos.property.application.port.in.ConfigureExistingPropertyUseCase;
import com.architek.oikos.property.application.port.in.ConfigurePropertyUseCase;
import com.architek.oikos.property.application.port.in.CreatePropertyUseCase;
import com.architek.oikos.property.application.port.in.GetPropertyUseCase;
import com.architek.oikos.property.application.port.in.ListPropertiesUseCase;
import com.architek.oikos.property.application.port.in.SetProjectedBudgetUseCase;
import com.architek.oikos.property.application.port.in.UpdateDuesCalculationModeUseCase;
import com.architek.oikos.property.application.port.in.UpdatePropertyUseCase;
import com.architek.oikos.property.domain.valueobject.DuesCalculationMode;
import com.architek.oikos.property.domain.valueobject.PropertyId;
import com.architek.oikos.shared.domain.pagination.Page;
import com.architek.oikos.shared.domain.valueobject.EntityId;
import com.architek.oikos.testsupport.WebSecuritySliceTestConfiguration;
import com.architek.oikos.user.application.port.in.AssignPropertyManagerUseCase;
import com.architek.oikos.user.application.port.in.GrantCreatorAsManagerUseCase;
import com.architek.oikos.user.application.usecase.EnforcePropertyCreationLimitService;

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
    private ConfigurePropertyUseCase configurePropertyUseCase;

    @MockitoBean
    private ConfigureExistingPropertyUseCase configureExistingPropertyUseCase;

    @MockitoBean
    private GetPropertyUseCase getPropertyUseCase;

    @MockitoBean
    private ListPropertiesUseCase listPropertiesUseCase;

    @MockitoBean
    private UpdatePropertyUseCase updatePropertyUseCase;

    @MockitoBean
    private UpdateDuesCalculationModeUseCase updateDuesCalculationModeUseCase;

    @MockitoBean
    private SetProjectedBudgetUseCase setProjectedBudgetUseCase;

    @MockitoBean
    private GrantCreatorAsManagerUseCase grantCreatorAsManagerUseCase;

    @MockitoBean
    private AssignPropertyManagerUseCase assignPropertyManagerUseCase;

    @MockitoBean
    private EnforcePropertyCreationLimitService enforcePropertyCreationLimitService;

    private String bearerToken(String... authorities) {
        return "Bearer " + jwtService.generateAccessToken(EntityId.of(UUID.randomUUID()), Set.of(authorities));
    }

    @Test
    void anonymous_request_is_rejected_with_401() throws Exception {
        mockMvc.perform(get("/api/v1/properties")).andExpect(status().isUnauthorized());
    }

    @Test
    void regular_user_with_no_managed_property_sees_an_empty_list() throws Exception {
        mockMvc.perform(get("/api/v1/properties").header("Authorization", bearerToken("ROLE_USER")))
                .andExpect(status().isOk());
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
        when(getPropertyUseCase.getProperty(any()))
                .thenReturn(new PropertyView(id, "Copro", "Address", DuesCalculationMode.FLAT_RATE, null));

        mockMvc.perform(get("/api/v1/properties/" + id).header("Authorization", bearerToken("ROLE_ADMIN")))
                .andExpect(status().isOk());
    }

    @Test
    void admin_can_create_a_property() throws Exception {
        PropertyId id = PropertyId.newId();
        when(createPropertyUseCase.create(any())).thenReturn(id);

        mockMvc.perform(post("/api/v1/properties")
                        .header("Authorization", bearerToken("ROLE_ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Copro Laumiere","address":"33 Avenue de Laumiere"}
                                """))
                .andExpect(status().isCreated());
    }

    @Test
    void regular_user_is_forbidden_from_creating_a_property() throws Exception {
        mockMvc.perform(post("/api/v1/properties")
                        .header("Authorization", bearerToken("ROLE_USER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Copro Laumiere","address":"33 Avenue de Laumiere"}
                                """))
                .andExpect(status().isForbidden());
    }

    @Test
    void create_with_a_blank_name_returns_400() throws Exception {
        mockMvc.perform(post("/api/v1/properties")
                        .header("Authorization", bearerToken("ROLE_ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":" ","address":"33 Avenue de Laumiere"}
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void admin_can_configure_a_property_with_its_buildings_and_unit_types() throws Exception {
        PropertyId id = PropertyId.newId();
        when(configurePropertyUseCase.configure(any())).thenReturn(id);

        mockMvc.perform(post("/api/v1/properties/configure")
                        .header("Authorization", bearerToken("ROLE_ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"property":{"name":"My Property","address":"123 Main St","buildings":[
                                    {"name":"Building A","floorCount":5,"unitTypes":[
                                        {"unitTypeName":"Appartement","count":50},
                                        {"unitTypeName":"Box","count":33}
                                    ]}
                                ]}}
                                """))
                .andExpect(status().isCreated());
    }

    @Test
    void configure_without_buildings_returns_400() throws Exception {
        mockMvc.perform(post("/api/v1/properties/configure")
                        .header("Authorization", bearerToken("ROLE_ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"property":{"name":"My Property","address":"123 Main St","buildings":[]}}
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void admin_can_update_a_property_name_and_address() throws Exception {
        PropertyId id = PropertyId.newId();
        when(updatePropertyUseCase.update(any()))
                .thenReturn(new PropertyView(id, "Copro Renamed", "New address", DuesCalculationMode.FLAT_RATE, null));

        mockMvc.perform(put("/api/v1/properties/" + id)
                        .header("Authorization", bearerToken("ROLE_ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Copro Renamed","address":"New address"}
                                """))
                .andExpect(status().isOk());
    }

    @Test
    void update_with_a_blank_name_returns_400() throws Exception {
        mockMvc.perform(put("/api/v1/properties/" + PropertyId.newId())
                        .header("Authorization", bearerToken("ROLE_ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":" ","address":"New address"}
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void admin_can_switch_the_dues_calculation_mode_to_shares() throws Exception {
        PropertyId id = PropertyId.newId();
        when(updateDuesCalculationModeUseCase.updateMode(any()))
                .thenReturn(new PropertyView(id, "Copro", "Address", DuesCalculationMode.SHARES, null));

        mockMvc.perform(put("/api/v1/properties/" + id + "/dues-calculation-mode")
                        .header("Authorization", bearerToken("ROLE_ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"mode":"SHARES"}
                                """))
                .andExpect(status().isOk());
    }

    @Test
    void admin_can_set_the_projected_budget() throws Exception {
        PropertyId id = PropertyId.newId();
        when(setProjectedBudgetUseCase.setProjectedBudget(any()))
                .thenReturn(new PropertyView(id, "Copro", "Address", DuesCalculationMode.SHARES, new java.math.BigDecimal("1000")));

        mockMvc.perform(put("/api/v1/properties/" + id + "/projected-budget")
                        .header("Authorization", bearerToken("ROLE_ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"projectedBudget":1000}
                                """))
                .andExpect(status().isOk());
    }

    @Test
    void setting_a_zero_projected_budget_returns_400() throws Exception {
        mockMvc.perform(put("/api/v1/properties/" + PropertyId.newId() + "/projected-budget")
                        .header("Authorization", bearerToken("ROLE_ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"projectedBudget":0}
                                """))
                .andExpect(status().isBadRequest());
    }

    private static final String CONFIGURATION_BODY = """
            {"duesCalculationMode":"FLAT_RATE",
             "unitTypes":[{"name":"Appartement","price":50}],
             "buildings":[{"unitTypes":[{"unitTypeName":"Appartement","count":12}]}],
             "bankAccounts":[{"label":"Attijariwafa Bank","bankAccountNumber":"0077800001234"}]}
            """;

    @Test
    void configuring_a_property_is_allowed_with_the_wizard_s_onboarding_token() throws Exception {
        PropertyId propertyId = PropertyId.newId();
        String onboardingToken = jwtService.generateOnboardingToken(EntityId.newId(),
                EntityId.of(propertyId.asUuid()));

        mockMvc.perform(post("/api/v1/properties/" + propertyId + "/configuration")
                        .header("Authorization", "Bearer " + onboardingToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(CONFIGURATION_BODY))
                .andExpect(status().isNoContent());
    }

    @Test
    void an_onboarding_token_issued_for_another_property_is_refused() throws Exception {
        String onboardingToken = jwtService.generateOnboardingToken(EntityId.newId(), EntityId.newId());

        mockMvc.perform(post("/api/v1/properties/" + PropertyId.newId() + "/configuration")
                        .header("Authorization", "Bearer " + onboardingToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(CONFIGURATION_BODY))
                .andExpect(status().isForbidden());
    }

    /** The token is a wizard credential, not a session: it must open nothing else. */
    @Test
    void an_onboarding_token_does_not_open_the_rest_of_the_property_api() throws Exception {
        PropertyId propertyId = PropertyId.newId();
        String onboardingToken = jwtService.generateOnboardingToken(EntityId.newId(),
                EntityId.of(propertyId.asUuid()));

        mockMvc.perform(get("/api/v1/properties/" + propertyId)
                        .header("Authorization", "Bearer " + onboardingToken))
                .andExpect(status().isForbidden());
    }

    @Test
    void configuring_a_property_is_also_allowed_with_an_ordinary_admin_session() throws Exception {
        mockMvc.perform(post("/api/v1/properties/" + PropertyId.newId() + "/configuration")
                        .header("Authorization", bearerToken("ROLE_ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(CONFIGURATION_BODY))
                .andExpect(status().isNoContent());
    }

    @Test
    void anonymous_configuration_is_rejected_with_401() throws Exception {
        mockMvc.perform(post("/api/v1/properties/" + PropertyId.newId() + "/configuration")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(CONFIGURATION_BODY))
                .andExpect(status().isUnauthorized());
    }
}
