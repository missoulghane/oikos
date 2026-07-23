package com.architek.oikos.user.web.controller;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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
import com.architek.oikos.shared.domain.valueobject.EntityId;
import com.architek.oikos.testsupport.WebSecuritySliceTestConfiguration;
import com.architek.oikos.user.application.dto.UserView;
import com.architek.oikos.user.application.port.in.ChangePasswordUseCase;
import com.architek.oikos.user.application.port.in.GetUserUseCase;
import com.architek.oikos.user.application.port.in.UpdateUserProfileUseCase;
import com.architek.oikos.user.domain.model.Role;
import com.architek.oikos.user.domain.valueobject.UserId;

/**
 * Since the app is stateless-JWT (rule: current-user identity always comes from the
 * signed access token, never from a session/repository), these tests authenticate
 * with a real token minted by the imported JwtService bean rather than @WithMockUser
 * - SessionCreationPolicy.STATELESS makes Spring Security use a NullSecurityContextRepository,
 * which @WithMockUser's context injection cannot survive.
 */
@WebMvcTest(controllers = UserMeController.class)
@Import(WebSecuritySliceTestConfiguration.class)
class UserMeControllerWebMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtService jwtService;

    @MockitoBean
    private GetUserUseCase getUserUseCase;

    @MockitoBean
    private UpdateUserProfileUseCase updateUserProfileUseCase;

    @MockitoBean
    private ChangePasswordUseCase changePasswordUseCase;

    private String bearerTokenFor(UUID userId) {
        return "Bearer " + jwtService.generateAccessToken(EntityId.of(userId), Set.of("ROLE_USER"));
    }

    @Test
    void anonymous_request_is_rejected_with_401() throws Exception {
        mockMvc.perform(get("/api/v1/users/me")).andExpect(status().isUnauthorized());
    }

    @Test
    void me_returns_the_authenticated_users_profile() throws Exception {
        UUID currentUserId = UUID.randomUUID();
        UserId userId = UserId.of(currentUserId);
        when(getUserUseCase.getUser(any())).thenReturn(
                new UserView(userId, null, "Jane Doe", "user@oikos.com", null, Set.of(Role.ROLE_USER), true, true));

        mockMvc.perform(get("/api/v1/users/me").header("Authorization", bearerTokenFor(currentUserId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email", is("user@oikos.com")));
    }

    @Test
    void update_profile_with_blank_full_name_returns_400() throws Exception {
        mockMvc.perform(patch("/api/v1/users/me/profile")
                        .header("Authorization", bearerTokenFor(UUID.randomUUID()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"fullName":"","email":"jane@doe.com"}
                                """))
                .andExpect(status().isBadRequest());
    }
}
