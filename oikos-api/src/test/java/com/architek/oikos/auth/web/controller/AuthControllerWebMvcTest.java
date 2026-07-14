package com.architek.oikos.auth.web.controller;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.architek.oikos.auth.application.dto.AuthTokens;
import com.architek.oikos.auth.application.port.in.LoginUseCase;
import com.architek.oikos.auth.application.port.in.LogoutUseCase;
import com.architek.oikos.auth.application.port.in.RefreshTokenUseCase;
import com.architek.oikos.auth.application.port.in.RequestPasswordResetUseCase;
import com.architek.oikos.auth.application.port.in.ResetPasswordUseCase;
import com.architek.oikos.auth.domain.exception.InvalidPasswordResetTokenException;
import com.architek.oikos.shared.exception.UnauthorizedException;

@WebMvcTest(controllers = AuthController.class)
class AuthControllerWebMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private LoginUseCase loginUseCase;

    @MockitoBean
    private RefreshTokenUseCase refreshTokenUseCase;

    @MockitoBean
    private LogoutUseCase logoutUseCase;

    @MockitoBean
    private RequestPasswordResetUseCase requestPasswordResetUseCase;

    @MockitoBean
    private ResetPasswordUseCase resetPasswordUseCase;

    @Test
    void login_returns_the_token_pair() throws Exception {
        when(loginUseCase.login(any())).thenReturn(new AuthTokens("access-token", "refresh-token", 900L));

        mockMvc.perform(post("/api/v1/auth/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"identifier":"user@oikos.com","password":"password123"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken", is("access-token")))
                .andExpect(jsonPath("$.refreshToken", is("refresh-token")))
                .andExpect(jsonPath("$.tokenType", is("Bearer")));
    }

    @Test
    void login_with_unverified_account_returns_403() throws Exception {
        when(loginUseCase.login(any())).thenThrow(new UnauthorizedException("Account is not verified"));

        mockMvc.perform(post("/api/v1/auth/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"identifier":"user@oikos.com","password":"password123"}
                                """))
                .andExpect(status().isForbidden());
    }

    @Test
    void login_with_invalid_body_returns_400() throws Exception {
        mockMvc.perform(post("/api/v1/auth/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"identifier":"not-an-email","password":""}
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void refresh_returns_a_new_token_pair() throws Exception {
        when(refreshTokenUseCase.refresh(any())).thenReturn(new AuthTokens("new-access", "new-refresh", 900L));

        mockMvc.perform(post("/api/v1/auth/refresh-token")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"refreshToken":"some-refresh-token"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken", is("new-access")));
    }

    @Test
    void logout_returns_204() throws Exception {
        mockMvc.perform(post("/api/v1/auth/logout")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"refreshToken":"some-refresh-token"}
                                """))
                .andExpect(status().isNoContent());
    }

    @Test
    void forgot_password_returns_202() throws Exception {
        mockMvc.perform(post("/api/v1/auth/forgot-password")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"user@oikos.com"}
                                """))
                .andExpect(status().isAccepted());
    }

    @Test
    void forgot_password_with_invalid_email_returns_400() throws Exception {
        mockMvc.perform(post("/api/v1/auth/forgot-password")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"not-an-email"}
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void reset_password_returns_200() throws Exception {
        mockMvc.perform(post("/api/v1/auth/reset-password")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"token":"some-token","newPassword":"newpassword1"}
                                """))
                .andExpect(status().isOk());
    }

    @Test
    void reset_password_with_invalid_token_returns_400() throws Exception {
        org.mockito.Mockito.doThrow(new InvalidPasswordResetTokenException("Invalid password reset token"))
                .when(resetPasswordUseCase).resetPassword(any());

        mockMvc.perform(post("/api/v1/auth/reset-password")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"token":"bad-token","newPassword":"newpassword1"}
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void reset_password_with_short_password_returns_400() throws Exception {
        mockMvc.perform(post("/api/v1/auth/reset-password")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"token":"some-token","newPassword":"short"}
                                """))
                .andExpect(status().isBadRequest());
    }
}
