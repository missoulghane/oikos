package com.architek.oikos.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Set;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import com.jayway.jsonpath.JsonPath;

import com.architek.oikos.auth.infrastructure.security.JwtService;
import com.architek.oikos.shared.application.port.out.EmailSenderPort;
import com.architek.oikos.shared.domain.valueobject.EntityId;

/**
 * Full-stack integration test: exercises the real Spring context (JWT filter,
 * SecurityConfiguration, H2 persistence) across the user + auth features -
 * register -> verify -> login -> access a protected resource -> refresh (rotation)
 * -> logout. EmailSenderPort is mocked since no real SMTP server is available in
 * tests; the verification token is recovered from the composed email body.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
class AuthenticationFlowIntegrationTest {

    private static final Pattern TOKEN_PATTERN = Pattern.compile("token=([^\"&]+)");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtService jwtService;

    @MockitoBean
    private EmailSenderPort emailSenderPort;

    private String adminBearerToken() {
        return "Bearer " + jwtService.generateAccessToken(EntityId.of(UUID.randomUUID()), Set.of("ROLE_ADMIN"));
    }

    @Test
    void register_verify_login_access_refresh_and_logout() throws Exception {
        String email = "flow-user@oikos.com";
        String password = "password123456";

        mockMvc.perform(post("/api/v1/users/register-property-user")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"fullName":"Jane Doe","email":"%s","password":"%s"}
                                """.formatted(email, password)))
                .andExpect(status().isCreated());

        var bodyCaptor = org.mockito.ArgumentCaptor.forClass(String.class);
        verify(emailSenderPort).send(any(), any(), bodyCaptor.capture());
        String token = extractToken(bodyCaptor.getValue());

        mockMvc.perform(post("/api/v1/users/verify")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"token":"%s"}
                                """.formatted(token)))
                .andExpect(status().isOk());

        MvcResult loginResult = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"identifier":"%s","password":"%s"}
                                """.formatted(email, password)))
                .andExpect(status().isOk())
                .andReturn();
        String loginBody = loginResult.getResponse().getContentAsString();
        String accessToken = JsonPath.read(loginBody, "$.accessToken");
        String refreshToken = JsonPath.read(loginBody, "$.refreshToken");

        mockMvc.perform(get("/api/v1/users/me").header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email", is(email)));

        mockMvc.perform(get("/api/v1/users/me"))
                .andExpect(status().isUnauthorized());

        MvcResult refreshResult = mockMvc.perform(post("/api/v1/auth/refresh-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"refreshToken":"%s"}
                                """.formatted(refreshToken)))
                .andExpect(status().isOk())
                .andReturn();
        String newRefreshToken = JsonPath.read(refreshResult.getResponse().getContentAsString(), "$.refreshToken");
        assertThat(newRefreshToken).isNotEqualTo(refreshToken);

        mockMvc.perform(post("/api/v1/auth/refresh-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"refreshToken":"%s"}
                                """.formatted(refreshToken)))
                .andExpect(status().isForbidden());

        mockMvc.perform(post("/api/v1/auth/logout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"refreshToken":"%s"}
                                """.formatted(newRefreshToken)))
                .andExpect(status().isNoContent());
    }

    @Test
    void registers_a_property_manager_with_its_property_and_logs_in_after_verification() throws Exception {
        String email = "flow-property-manager@oikos.com";
        String password = "password123456";

        mockMvc.perform(post("/api/v1/users/register-property-manager")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"fullName":"Jane Doe","email":"%s","password":"%s",
                                "propertyName":"Residence A","propertyAddress":"1 rue de Paris"}
                                """.formatted(email, password)))
                .andExpect(status().isCreated());

        var bodyCaptor = org.mockito.ArgumentCaptor.forClass(String.class);
        verify(emailSenderPort).send(any(), any(), bodyCaptor.capture());
        String token = extractToken(bodyCaptor.getValue());

        mockMvc.perform(post("/api/v1/users/verify")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"token":"%s"}
                                """.formatted(token)))
                .andExpect(status().isOk());

        MvcResult loginResult = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"identifier":"%s","password":"%s"}
                                """.formatted(email, password)))
                .andExpect(status().isOk())
                .andReturn();
        String accessToken = JsonPath.read(loginResult.getResponse().getContentAsString(), "$.accessToken");

        mockMvc.perform(get("/api/v1/users/me").header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email", is(email)))
                .andExpect(jsonPath("$.roles", org.hamcrest.Matchers.hasItem("ROLE_PROPERTY_MANAGER")));
    }

    @Test
    void logs_in_with_login_email_or_phone_indifferently() throws Exception {
        String email = "multi-identifier-user@oikos.com";
        String phone = "0611223344";
        String login = "multi-id-login";
        String password = "password123456";

        mockMvc.perform(post("/api/v1/users/register-property-user")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"fullName":"Jane Doe","email":"%s","phone":"%s","login":"%s","password":"%s"}
                                """.formatted(email, phone, login, password)))
                .andExpect(status().isCreated());

        var bodyCaptor = org.mockito.ArgumentCaptor.forClass(String.class);
        verify(emailSenderPort).send(any(), any(), bodyCaptor.capture());
        mockMvc.perform(post("/api/v1/users/verify")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"token":"%s"}
                                """.formatted(extractToken(bodyCaptor.getValue()))))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"identifier":"%s","password":"%s"}
                                """.formatted(login, password)))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"identifier":"%s","password":"%s"}
                                """.formatted(email, password)))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"identifier":"%s","password":"%s"}
                                """.formatted(phone, password)))
                .andExpect(status().isOk());
    }

    @Test
    void forgot_password_reset_password_and_login_with_the_new_password() throws Exception {
        String email = "forgot-password-user@oikos.com";
        String oldPassword = "oldpassword123";
        String newPassword = "newpassword456";

        mockMvc.perform(post("/api/v1/users/register-property-user")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"fullName":"Jane Doe","email":"%s","password":"%s"}
                                """.formatted(email, oldPassword)))
                .andExpect(status().isCreated());

        var verificationBodyCaptor = org.mockito.ArgumentCaptor.forClass(String.class);
        verify(emailSenderPort).send(any(), any(), verificationBodyCaptor.capture());
        mockMvc.perform(post("/api/v1/users/verify")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"token":"%s"}
                                """.formatted(extractToken(verificationBodyCaptor.getValue()))))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/v1/auth/forgot-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"%s"}
                                """.formatted(email)))
                .andExpect(status().isAccepted());

        var resetBodyCaptor = org.mockito.ArgumentCaptor.forClass(String.class);
        verify(emailSenderPort, org.mockito.Mockito.times(2)).send(any(), any(), resetBodyCaptor.capture());
        String resetToken = extractToken(resetBodyCaptor.getValue());

        mockMvc.perform(post("/api/v1/auth/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"token":"%s","newPassword":"%s"}
                                """.formatted(resetToken, newPassword)))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"identifier":"%s","password":"%s"}
                                """.formatted(email, oldPassword)))
                .andExpect(status().isForbidden());

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"identifier":"%s","password":"%s"}
                                """.formatted(email, newPassword)))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/v1/auth/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"token":"%s","newPassword":"anotherpassword1"}
                                """.formatted(resetToken)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void forgot_password_for_an_unknown_email_is_silently_accepted() throws Exception {
        mockMvc.perform(post("/api/v1/auth/forgot-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"no-such-account@oikos.com"}
                                """))
                .andExpect(status().isAccepted());
    }

    @Test
    void login_before_account_verification_is_rejected_with_403() throws Exception {
        String email = "unverified-user@oikos.com";
        String password = "password123456";

        mockMvc.perform(post("/api/v1/users/register-property-user")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"fullName":"Jane Doe","email":"%s","password":"%s"}
                                """.formatted(email, password)))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"identifier":"%s","password":"%s"}
                                """.formatted(email, password)))
                .andExpect(status().isForbidden());
    }

    @Test
    void login_with_a_disabled_account_is_rejected_with_403() throws Exception {
        String email = "disabled-user@oikos.com";
        String password = "password123456";

        MvcResult registerResult = mockMvc.perform(post("/api/v1/users/register-property-user")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"fullName":"Jane Doe","email":"%s","password":"%s"}
                                """.formatted(email, password)))
                .andExpect(status().isCreated())
                .andReturn();
        String userId = registerResult.getResponse().getHeader("Location").replace("/api/v1/users/", "");

        var bodyCaptor = org.mockito.ArgumentCaptor.forClass(String.class);
        verify(emailSenderPort).send(any(), any(), bodyCaptor.capture());
        String token = extractToken(bodyCaptor.getValue());

        mockMvc.perform(post("/api/v1/users/verify")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"token":"%s"}
                                """.formatted(token)))
                .andExpect(status().isOk());

        mockMvc.perform(patch("/api/v1/users/" + userId + "/status")
                        .header("Authorization", adminBearerToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"enabled":false}
                                """))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"identifier":"%s","password":"%s"}
                                """.formatted(email, password)))
                .andExpect(status().isForbidden());

        mockMvc.perform(patch("/api/v1/users/" + userId + "/status")
                        .header("Authorization", adminBearerToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"enabled":true}
                                """))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"identifier":"%s","password":"%s"}
                                """.formatted(email, password)))
                .andExpect(status().isOk());
    }

    @Test
    void admin_creates_a_user_who_activates_the_account_and_logs_in() throws Exception {
        String email = "invited-user@oikos.com";
        String chosenPassword = "chosenpassword123";

        mockMvc.perform(post("/api/v1/users")
                        .header("Authorization", adminBearerToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"fullName":"Jane Doe","email":"%s"}
                                """.formatted(email)))
                .andExpect(status().isCreated());

        // Not yet activated: login must be rejected even with any password.
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"identifier":"%s","password":"whatever12345"}
                                """.formatted(email)))
                .andExpect(status().isForbidden());

        var bodyCaptor = org.mockito.ArgumentCaptor.forClass(String.class);
        verify(emailSenderPort).send(any(), any(), bodyCaptor.capture());
        String token = extractToken(bodyCaptor.getValue());

        mockMvc.perform(post("/api/v1/users/activate-account")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"token":"%s","newPassword":"%s"}
                                """.formatted(token, chosenPassword)))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"identifier":"%s","password":"%s"}
                                """.formatted(email, chosenPassword)))
                .andExpect(status().isOk());

        // The single-use token cannot be replayed.
        mockMvc.perform(post("/api/v1/users/activate-account")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"token":"%s","newPassword":"anotherpassword1"}
                                """.formatted(token)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void admin_can_resend_the_activation_email_and_the_new_token_supersedes_the_old_one() throws Exception {
        String email = "resend-invite-user@oikos.com";

        MvcResult createResult = mockMvc.perform(post("/api/v1/users")
                        .header("Authorization", adminBearerToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"fullName":"Jane Doe","email":"%s"}
                                """.formatted(email)))
                .andExpect(status().isCreated())
                .andReturn();
        String userId = createResult.getResponse().getHeader("Location").replace("/api/v1/users/", "");

        var firstEmailCaptor = org.mockito.ArgumentCaptor.forClass(String.class);
        verify(emailSenderPort).send(any(), any(), firstEmailCaptor.capture());
        String firstToken = extractToken(firstEmailCaptor.getValue());

        mockMvc.perform(post("/api/v1/users/" + userId + "/resend-activation")
                        .header("Authorization", adminBearerToken()))
                .andExpect(status().isAccepted());

        var secondEmailCaptor = org.mockito.ArgumentCaptor.forClass(String.class);
        verify(emailSenderPort, org.mockito.Mockito.times(2)).send(any(), any(), secondEmailCaptor.capture());
        String secondToken = extractToken(secondEmailCaptor.getValue());

        // The original token was invalidated by the resend.
        mockMvc.perform(post("/api/v1/users/activate-account")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"token":"%s","newPassword":"chosenpassword123"}
                                """.formatted(firstToken)))
                .andExpect(status().isBadRequest());

        mockMvc.perform(post("/api/v1/users/activate-account")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"token":"%s","newPassword":"chosenpassword123"}
                                """.formatted(secondToken)))
                .andExpect(status().isOk());

        // Resending after activation is rejected.
        mockMvc.perform(post("/api/v1/users/" + userId + "/resend-activation")
                        .header("Authorization", adminBearerToken()))
                .andExpect(status().isBadRequest());
    }

    private static String extractToken(String htmlBody) {
        Matcher matcher = TOKEN_PATTERN.matcher(htmlBody);
        if (!matcher.find()) {
            throw new IllegalStateException("verification token not found in email body: " + htmlBody);
        }
        return matcher.group(1);
    }
}
