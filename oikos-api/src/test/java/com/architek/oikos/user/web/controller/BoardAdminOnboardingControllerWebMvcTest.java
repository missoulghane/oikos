package com.architek.oikos.user.web.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Set;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.architek.oikos.auth.infrastructure.security.JwtService;
import com.architek.oikos.shared.domain.valueobject.EntityId;
import com.architek.oikos.testsupport.WebSecuritySliceTestConfiguration;
import com.architek.oikos.user.application.port.in.CreateBoardAdminPropertyUseCase;
import com.architek.oikos.user.domain.exception.BoardAdminPropertyAlreadyExistsException;
import com.architek.oikos.user.domain.valueobject.UserId;

@WebMvcTest(controllers = BoardAdminOnboardingController.class)
@Import(WebSecuritySliceTestConfiguration.class)
class BoardAdminOnboardingControllerWebMvcTest {

    private static final String BODY = """
            {"propertyName":"Residence A","propertyAddress":"1 rue de Paris","propertyCity":"Casablanca",
             "boardRole":"VOLUNTEER_MANAGER"}
            """;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtService jwtService;

    @MockitoBean
    private CreateBoardAdminPropertyUseCase createBoardAdminPropertyUseCase;

    @Test
    void the_account_onboarding_token_of_step_1_creates_the_property_and_gets_a_property_scoped_token_back()
            throws Exception {
        EntityId propertyId = EntityId.newId();
        when(createBoardAdminPropertyUseCase.create(any())).thenReturn(propertyId);
        String accountToken = jwtService.generateAccountOnboardingToken(EntityId.newId());

        mockMvc.perform(post("/api/v1/users/me/board-admin-property")
                        .header("Authorization", "Bearer " + accountToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(BODY))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/v1/properties/" + propertyId))
                .andExpect(jsonPath("$.propertyId").value(propertyId.toString()))
                .andExpect(jsonPath("$.onboardingToken").isNotEmpty());
    }

    /** A visitor who left after step 1 resumes there once verified and logged in. */
    @Test
    void an_ordinary_session_may_create_the_property_too() throws Exception {
        when(createBoardAdminPropertyUseCase.create(any())).thenReturn(EntityId.newId());
        String accessToken = jwtService.generateAccessToken(EntityId.newId(), Set.of("ROLE_USER"));

        mockMvc.perform(post("/api/v1/users/me/board-admin-property")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(BODY))
                .andExpect(status().isCreated());
    }

    @Test
    void a_property_scoped_onboarding_token_is_refused() throws Exception {
        String propertyToken = jwtService.generateOnboardingToken(EntityId.newId(), EntityId.newId());

        mockMvc.perform(post("/api/v1/users/me/board-admin-property")
                        .header("Authorization", "Bearer " + propertyToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(BODY))
                .andExpect(status().isForbidden());
    }

    @Test
    void anonymous_request_is_rejected_with_401() throws Exception {
        mockMvc.perform(post("/api/v1/users/me/board-admin-property")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(BODY))
                .andExpect(status().isUnauthorized());
    }

    /** PROPERTY_MANAGER belongs to a professional firm: not a seat the wizard offers. */
    @Test
    void a_seat_outside_the_wizard_s_choices_returns_400() throws Exception {
        String accountToken = jwtService.generateAccountOnboardingToken(EntityId.newId());

        mockMvc.perform(post("/api/v1/users/me/board-admin-property")
                        .header("Authorization", "Bearer " + accountToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"propertyName":"Residence A","propertyAddress":"1 rue de Paris",
                                 "boardRole":"PROPERTY_MANAGER"}
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void replaying_step_2_for_an_account_that_already_has_its_property_returns_409() throws Exception {
        when(createBoardAdminPropertyUseCase.create(any()))
                .thenThrow(new BoardAdminPropertyAlreadyExistsException(UserId.newId()));
        String accountToken = jwtService.generateAccountOnboardingToken(EntityId.newId());

        mockMvc.perform(post("/api/v1/users/me/board-admin-property")
                        .header("Authorization", "Bearer " + accountToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(BODY))
                .andExpect(status().isConflict());
    }
}
