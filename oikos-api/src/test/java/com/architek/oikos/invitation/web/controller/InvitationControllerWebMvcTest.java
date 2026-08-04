package com.architek.oikos.invitation.web.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
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
import com.architek.oikos.invitation.application.dto.InvitationView;
import com.architek.oikos.invitation.application.port.in.CreateInvitationUseCase;
import com.architek.oikos.invitation.application.port.in.DisableInvitationUseCase;
import com.architek.oikos.invitation.application.port.in.GetInvitationUseCase;
import com.architek.oikos.invitation.application.port.in.ListInvitationsUseCase;
import com.architek.oikos.invitation.domain.model.InvitationStatus;
import com.architek.oikos.invitation.domain.model.InvitationType;
import com.architek.oikos.invitation.domain.valueobject.InvitationId;
import com.architek.oikos.shared.domain.pagination.Page;
import com.architek.oikos.shared.domain.valueobject.EntityId;
import com.architek.oikos.testsupport.WebSecuritySliceTestConfiguration;
import com.architek.oikos.user.application.dto.UserAccessView;
import com.architek.oikos.user.application.port.in.GetUserAccessUseCase;
import com.architek.oikos.user.domain.model.Permission;

@WebMvcTest(controllers = InvitationController.class)
@Import(WebSecuritySliceTestConfiguration.class)
class InvitationControllerWebMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private GetUserAccessUseCase getUserAccessUseCase;

    @MockitoBean
    private CreateInvitationUseCase createInvitationUseCase;

    @MockitoBean
    private ListInvitationsUseCase listInvitationsUseCase;

    @MockitoBean
    private GetInvitationUseCase getInvitationUseCase;

    @MockitoBean
    private DisableInvitationUseCase disableInvitationUseCase;

    private String bearerToken(String... authorities) {
        return "Bearer " + jwtService.generateAccessToken(EntityId.of(UUID.randomUUID()), Set.of(authorities));
    }

    private InvitationView sampleView(String propertyId) {
        return new InvitationView(InvitationId.newId(), EntityId.of(propertyId), InvitationType.PUBLIC, "PROPERTY_OWNER",
                null, null, "http://localhost/invitations?token=tok", InvitationStatus.ACTIVE,
                Instant.EPOCH.plus(Duration.ofDays(30)), EntityId.newId());
    }

    @Test
    void anonymous_request_is_rejected_with_401() throws Exception {
        mockMvc.perform(get("/api/v1/properties/" + UUID.randomUUID() + "/invitations"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void a_user_without_the_invitation_manage_permission_is_forbidden_from_creating() throws Exception {
        String propertyId = UUID.randomUUID().toString();

        mockMvc.perform(post("/api/v1/properties/" + propertyId + "/invitations")
                        .header("Authorization", bearerToken("ROLE_USER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"type":"PUBLIC"}
                                """))
                .andExpect(status().isForbidden());
    }

    @Test
    void a_manager_with_the_invitation_manage_permission_can_create_a_public_invitation() throws Exception {
        String propertyId = UUID.randomUUID().toString();
        when(getUserAccessUseCase.getAccess(any())).thenReturn(new UserAccessView(
                Set.of(), Map.of(), Map.of(propertyId, Set.of(Permission.INVITATION_MANAGE)), Set.of(), Set.of()));
        when(createInvitationUseCase.create(any())).thenReturn(InvitationId.newId());

        mockMvc.perform(post("/api/v1/properties/" + propertyId + "/invitations")
                        .header("Authorization", bearerToken("PROPERTY_MANAGER_ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"type":"PUBLIC"}
                                """))
                .andExpect(status().isCreated());
    }

    @Test
    void a_manager_with_the_invitation_manage_permission_can_list_invitations() throws Exception {
        String propertyId = UUID.randomUUID().toString();
        when(getUserAccessUseCase.getAccess(any())).thenReturn(new UserAccessView(
                Set.of(), Map.of(), Map.of(propertyId, Set.of(Permission.INVITATION_MANAGE)), Set.of(), Set.of()));
        when(listInvitationsUseCase.listInvitations(any())).thenReturn(Page.of(List.of(sampleView(propertyId)), 0, 20, 1));

        mockMvc.perform(get("/api/v1/properties/" + propertyId + "/invitations")
                        .header("Authorization", bearerToken("PROPERTY_MANAGER_ADMIN")))
                .andExpect(status().isOk());
    }

    @Test
    void a_manager_with_the_invitation_manage_permission_can_get_and_disable_an_invitation() throws Exception {
        String propertyId = UUID.randomUUID().toString();
        InvitationView view = sampleView(propertyId);
        when(getInvitationUseCase.getInvitation(any())).thenReturn(view);
        when(getUserAccessUseCase.getAccess(any())).thenReturn(new UserAccessView(
                Set.of(), Map.of(), Map.of(propertyId, Set.of(Permission.INVITATION_MANAGE)), Set.of(), Set.of()));

        mockMvc.perform(get("/api/v1/invitations/" + view.id())
                        .header("Authorization", bearerToken("PROPERTY_MANAGER_ADMIN")))
                .andExpect(status().isOk());

        mockMvc.perform(patch("/api/v1/invitations/" + view.id() + "/disable")
                        .header("Authorization", bearerToken("PROPERTY_MANAGER_ADMIN")))
                .andExpect(status().isNoContent());
    }
}
