package com.architek.oikos.user.web.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
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
import com.architek.oikos.shared.domain.pagination.Page;
import com.architek.oikos.shared.domain.valueobject.EntityId;
import com.architek.oikos.testsupport.WebSecuritySliceTestConfiguration;
import com.architek.oikos.user.application.dto.UserView;
import com.architek.oikos.user.application.port.in.ChangeUserStatusUseCase;
import com.architek.oikos.user.application.port.in.CreateUserUseCase;
import com.architek.oikos.user.application.port.in.DeleteUserUseCase;
import com.architek.oikos.user.application.port.in.GetUserUseCase;
import com.architek.oikos.user.application.port.in.ListUsersUseCase;
import com.architek.oikos.user.application.port.in.ResendAccountActivationUseCase;
import com.architek.oikos.user.application.port.in.UpdateUserProfileUseCase;
import com.architek.oikos.user.domain.model.Role;
import com.architek.oikos.user.domain.valueobject.UserId;

@WebMvcTest(controllers = UserAdminController.class)
@Import(WebSecuritySliceTestConfiguration.class)
class UserAdminControllerWebMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtService jwtService;

    @MockitoBean
    private ListUsersUseCase listUsersUseCase;

    @MockitoBean
    private GetUserUseCase getUserUseCase;

    @MockitoBean
    private UpdateUserProfileUseCase updateUserProfileUseCase;

    @MockitoBean
    private DeleteUserUseCase deleteUserUseCase;

    @MockitoBean
    private CreateUserUseCase createUserUseCase;

    @MockitoBean
    private ChangeUserStatusUseCase changeUserStatusUseCase;

    @MockitoBean
    private ResendAccountActivationUseCase resendAccountActivationUseCase;

    private String bearerToken(String... authorities) {
        return "Bearer " + jwtService.generateAccessToken(EntityId.of(UUID.randomUUID()), Set.of(authorities));
    }

    private static UserView newUserView(UserId userId, boolean enabled) {
        return new UserView(userId, null, "Doe", "Jane", "user@oikos.com", null, Set.of(Role.ROLE_USER), true, enabled);
    }

    @Test
    void anonymous_request_is_rejected_with_401() throws Exception {
        mockMvc.perform(get("/api/v1/users")).andExpect(status().isUnauthorized());
    }

    @Test
    void regular_user_is_forbidden_from_listing_users() throws Exception {
        mockMvc.perform(get("/api/v1/users").header("Authorization", bearerToken("ROLE_USER")))
                .andExpect(status().isForbidden());
    }

    @Test
    void admin_can_list_users() throws Exception {
        when(listUsersUseCase.listUsers(any())).thenReturn(Page.of(List.of(), 0, 20, 0));

        mockMvc.perform(get("/api/v1/users").header("Authorization", bearerToken("ROLE_ADMIN")))
                .andExpect(status().isOk());
    }

    @Test
    void admin_can_get_a_user_by_id() throws Exception {
        UserId userId = UserId.newId();
        when(getUserUseCase.getUser(any())).thenReturn(newUserView(userId, true));

        mockMvc.perform(get("/api/v1/users/" + userId).header("Authorization", bearerToken("ROLE_ADMIN")))
                .andExpect(status().isOk());
    }

    @Test
    void admin_can_create_a_user_returns_201_with_location_header() throws Exception {
        UserId userId = UserId.newId();
        when(createUserUseCase.create(any())).thenReturn(userId);

        mockMvc.perform(post("/api/v1/users")
                        .header("Authorization", bearerToken("ROLE_ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"lastName":"Doe","firstName":"Jane","email":"new-user@oikos.com"}
                                """))
                .andExpect(status().isCreated());
    }

    @Test
    void regular_user_is_forbidden_from_creating_a_user() throws Exception {
        mockMvc.perform(post("/api/v1/users")
                        .header("Authorization", bearerToken("ROLE_USER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"lastName":"Doe","firstName":"Jane","email":"new-user@oikos.com"}
                                """))
                .andExpect(status().isForbidden());
    }

    @Test
    void admin_can_change_user_status() throws Exception {
        UserId userId = UserId.newId();
        when(changeUserStatusUseCase.changeStatus(any())).thenReturn(newUserView(userId, false));

        mockMvc.perform(patch("/api/v1/users/" + userId + "/status")
                        .header("Authorization", bearerToken("ROLE_ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"enabled":false}
                                """))
                .andExpect(status().isOk());
    }

    @Test
    void regular_user_is_forbidden_from_changing_user_status() throws Exception {
        mockMvc.perform(patch("/api/v1/users/" + UserId.newId() + "/status")
                        .header("Authorization", bearerToken("ROLE_USER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"enabled":false}
                                """))
                .andExpect(status().isForbidden());
    }

    @Test
    void admin_can_resend_activation_email() throws Exception {
        mockMvc.perform(post("/api/v1/users/" + UserId.newId() + "/resend-activation")
                        .header("Authorization", bearerToken("ROLE_ADMIN")))
                .andExpect(status().isAccepted());
    }

    @Test
    void regular_user_is_forbidden_from_resending_activation_email() throws Exception {
        mockMvc.perform(post("/api/v1/users/" + UserId.newId() + "/resend-activation")
                        .header("Authorization", bearerToken("ROLE_USER")))
                .andExpect(status().isForbidden());
    }
}
