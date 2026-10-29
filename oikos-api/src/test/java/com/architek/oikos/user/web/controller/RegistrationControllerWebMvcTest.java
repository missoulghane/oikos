package com.architek.oikos.user.web.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.architek.oikos.user.application.port.in.AcceptPartyInvitationUseCase;
import com.architek.oikos.user.application.port.in.ActivateAccountUseCase;
import com.architek.oikos.auth.infrastructure.security.JwtService;
import com.architek.oikos.user.application.port.in.RegisterPropertyBoardAdminUseCase;
import com.architek.oikos.user.application.port.in.RegisterPropertyManagerAdminUseCase;
import com.architek.oikos.user.application.port.in.RegisterUserUseCase;
import com.architek.oikos.user.application.port.in.ResendVerificationUseCase;
import com.architek.oikos.user.application.port.in.VerifyAccountUseCase;
import com.architek.oikos.user.domain.exception.InvalidVerificationTokenException;
import com.architek.oikos.user.domain.valueobject.UserId;

// The real JwtService rather than a mock: the board-admin response must carry an
// actually signed onboarding token, which is the only credential the wizard has
// until the account is verified.
@WebMvcTest(controllers = RegistrationController.class)
@Import(JwtService.class)
class RegistrationControllerWebMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private RegisterUserUseCase registerUserUseCase;

    @MockitoBean
    private RegisterPropertyBoardAdminUseCase registerPropertyBoardAdminUseCase;

    @MockitoBean
    private RegisterPropertyManagerAdminUseCase registerPropertyManagerAdminUseCase;

    @MockitoBean
    private VerifyAccountUseCase verifyAccountUseCase;

    @MockitoBean
    private ResendVerificationUseCase resendVerificationUseCase;

    @MockitoBean
    private ActivateAccountUseCase activateAccountUseCase;

    @MockitoBean
    private AcceptPartyInvitationUseCase acceptPartyInvitationUseCase;

    @Test
    void register_with_valid_body_returns_202_without_location() throws Exception {
        mockMvc.perform(post("/api/v1/users/register-property-user")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"fullName":"Jane Doe","email":"user@oikos.com","password":"password123"}
                                """))
                // Same answer for a known address: no Location, which would name a new account.
                .andExpect(status().isAccepted())
                .andExpect(header().doesNotExist("Location"));
    }

    @Test
    void register_with_valid_role_returns_202() throws Exception {
        mockMvc.perform(post("/api/v1/users/register-property-user")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"fullName":"Jane Doe","email":"user@oikos.com","password":"password123","role":"ROLE_USER"}
                                """))
                .andExpect(status().isAccepted());
    }

    @Test
    void register_with_unknown_role_returns_400() throws Exception {
        mockMvc.perform(post("/api/v1/users/register-property-user")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"fullName":"Jane Doe","email":"user@oikos.com","password":"password123","role":"NOT_A_ROLE"}
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void register_with_invalid_email_returns_400() throws Exception {
        mockMvc.perform(post("/api/v1/users/register-property-user")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"fullName":"Jane Doe","email":"not-an-email","password":"password123"}
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void register_with_short_password_returns_400() throws Exception {
        mockMvc.perform(post("/api/v1/users/register-property-user")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"fullName":"Jane Doe","email":"user@oikos.com","password":"short"}
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void register_property_board_admin_creates_the_account_alone_and_returns_an_account_onboarding_token() throws Exception {
        UserId userId = UserId.newId();
        when(registerPropertyBoardAdminUseCase.register(any())).thenReturn(userId);

        mockMvc.perform(post("/api/v1/users/register-property-board-admin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"fullName":"Jane Doe","email":"board-admin@oikos.com","password":"password123",
                                "acceptedTerms":true}
                                """))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/v1/users/" + userId))
                .andExpect(jsonPath("$.userId").value(userId.toString()))
                // The wizard cannot log in yet (the account is unverified), so the token
                // is the only thing letting step 2 create the account's property.
                .andExpect(jsonPath("$.onboardingToken").isNotEmpty())
                .andExpect(jsonPath("$.expiresInSeconds").isNumber());
    }

    @Test
    void register_property_board_admin_without_accepting_the_terms_returns_400() throws Exception {
        mockMvc.perform(post("/api/v1/users/register-property-board-admin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"fullName":"Jane Doe","email":"board-admin@oikos.com","password":"password123",
                                "acceptedTerms":false}
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void register_property_board_admin_with_the_terms_field_missing_returns_400() throws Exception {
        mockMvc.perform(post("/api/v1/users/register-property-board-admin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"fullName":"Jane Doe","email":"board-admin@oikos.com","password":"password123"}
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void register_property_manager_admin_with_valid_body_returns_202_without_location() throws Exception {
        mockMvc.perform(post("/api/v1/users/register-property-manager-admin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"fullName":"Jane Doe","email":"manager-admin@oikos.com","password":"password123",
                                "propertyName":"Residence A","propertyAddress":"1 rue de Paris"}
                                """))
                .andExpect(status().isAccepted())
                .andExpect(header().doesNotExist("Location"));
    }

    @Test
    void register_property_manager_admin_with_missing_property_fields_returns_400() throws Exception {
        mockMvc.perform(post("/api/v1/users/register-property-manager-admin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"fullName":"Jane Doe","email":"manager-admin@oikos.com","password":"password123"}
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void verify_with_valid_token_returns_200() throws Exception {
        mockMvc.perform(post("/api/v1/users/verify")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"token":"some-token"}
                                """))
                .andExpect(status().isOk());
    }

    @Test
    void verify_with_invalid_token_returns_400() throws Exception {
        org.mockito.Mockito.doThrow(new InvalidVerificationTokenException("bad token"))
                .when(verifyAccountUseCase).verify(any());

        mockMvc.perform(post("/api/v1/users/verify")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"token":"bad-token"}
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void resend_verification_always_returns_202() throws Exception {
        mockMvc.perform(post("/api/v1/users/resend-verification")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"unknown@oikos.com"}
                                """))
                .andExpect(status().isAccepted());
    }

    @Test
    void activate_account_with_valid_token_and_password_returns_200() throws Exception {
        mockMvc.perform(post("/api/v1/users/activate-account")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"token":"some-token","newPassword":"password123"}
                                """))
                .andExpect(status().isOk());
    }

    @Test
    void activate_account_with_invalid_token_returns_400() throws Exception {
        org.mockito.Mockito.doThrow(new InvalidVerificationTokenException("bad token"))
                .when(activateAccountUseCase).activate(any());

        mockMvc.perform(post("/api/v1/users/activate-account")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"token":"bad-token","newPassword":"password123"}
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void activate_account_with_short_password_returns_400() throws Exception {
        mockMvc.perform(post("/api/v1/users/activate-account")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"token":"some-token","newPassword":"short"}
                                """))
                .andExpect(status().isBadRequest());
    }
}
