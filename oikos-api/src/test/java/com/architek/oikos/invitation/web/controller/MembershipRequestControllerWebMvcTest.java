package com.architek.oikos.invitation.web.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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
import com.architek.oikos.invitation.application.dto.MembershipRequestView;
import com.architek.oikos.invitation.application.port.in.AcceptMembershipRequestUseCase;
import com.architek.oikos.invitation.application.port.in.GetMembershipRequestUseCase;
import com.architek.oikos.invitation.application.port.in.ListMembershipRequestsUseCase;
import com.architek.oikos.invitation.application.port.in.RejectMembershipRequestUseCase;
import com.architek.oikos.invitation.domain.model.MembershipRequestStatus;
import com.architek.oikos.invitation.domain.valueobject.MembershipRequestId;
import com.architek.oikos.shared.domain.pagination.Page;
import com.architek.oikos.shared.domain.valueobject.EntityId;
import com.architek.oikos.testsupport.WebSecuritySliceTestConfiguration;
import com.architek.oikos.user.application.dto.UserAccessView;
import com.architek.oikos.user.application.port.in.GetUserAccessUseCase;
import com.architek.oikos.user.domain.model.Permission;

@WebMvcTest(controllers = MembershipRequestController.class)
@Import(WebSecuritySliceTestConfiguration.class)
class MembershipRequestControllerWebMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private GetUserAccessUseCase getUserAccessUseCase;

    @MockitoBean
    private ListMembershipRequestsUseCase listMembershipRequestsUseCase;

    @MockitoBean
    private AcceptMembershipRequestUseCase acceptMembershipRequestUseCase;

    @MockitoBean
    private RejectMembershipRequestUseCase rejectMembershipRequestUseCase;

    @MockitoBean
    private GetMembershipRequestUseCase getMembershipRequestUseCase;

    private String bearerToken(String... authorities) {
        return "Bearer " + jwtService.generateAccessToken(EntityId.of(UUID.randomUUID()), Set.of(authorities));
    }

    private MembershipRequestView sampleView(String propertyId) {
        return new MembershipRequestView(MembershipRequestId.newId(), EntityId.newId(), EntityId.of(propertyId),
                EntityId.newId(), EntityId.newId(), EntityId.newId(), MembershipRequestStatus.PENDING, null, null, null);
    }

    @Test
    void anonymous_request_is_rejected_with_401() throws Exception {
        mockMvc.perform(get("/api/v1/properties/" + UUID.randomUUID() + "/membership-requests"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void a_user_without_the_invitation_manage_permission_is_forbidden_from_listing() throws Exception {
        String propertyId = UUID.randomUUID().toString();

        mockMvc.perform(get("/api/v1/properties/" + propertyId + "/membership-requests")
                        .header("Authorization", bearerToken("ROLE_USER")))
                .andExpect(status().isForbidden());
    }

    @Test
    void a_manager_with_the_invitation_manage_permission_can_list_membership_requests() throws Exception {
        String propertyId = UUID.randomUUID().toString();
        when(getUserAccessUseCase.getAccess(any())).thenReturn(new UserAccessView(
                Set.of(), Map.of(), Map.of(propertyId, Set.of(Permission.INVITATION_MANAGE)), Set.of(), Set.of()));
        when(listMembershipRequestsUseCase.listMembershipRequests(any()))
                .thenReturn(Page.of(List.of(sampleView(propertyId)), 0, 20, 1));

        mockMvc.perform(get("/api/v1/properties/" + propertyId + "/membership-requests")
                        .header("Authorization", bearerToken("PROPERTY_MANAGER_ADMIN")))
                .andExpect(status().isOk());
    }

    @Test
    void a_manager_with_the_invitation_manage_permission_can_accept_and_reject_a_membership_request() throws Exception {
        String propertyId = UUID.randomUUID().toString();
        MembershipRequestView view = sampleView(propertyId);
        when(getMembershipRequestUseCase.getMembershipRequest(any())).thenReturn(view);
        when(getUserAccessUseCase.getAccess(any())).thenReturn(new UserAccessView(
                Set.of(), Map.of(), Map.of(propertyId, Set.of(Permission.INVITATION_MANAGE)), Set.of(), Set.of()));

        mockMvc.perform(patch("/api/v1/membership-requests/" + view.id() + "/accept")
                        .header("Authorization", bearerToken("PROPERTY_MANAGER_ADMIN")))
                .andExpect(status().isNoContent());

        mockMvc.perform(patch("/api/v1/membership-requests/" + view.id() + "/reject")
                        .header("Authorization", bearerToken("PROPERTY_MANAGER_ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"reason":"Lot déjà attribué"}
                                """))
                .andExpect(status().isNoContent());
    }

    @Test
    void a_user_without_the_invitation_manage_permission_is_forbidden_from_accepting() throws Exception {
        String propertyId = UUID.randomUUID().toString();
        MembershipRequestView view = sampleView(propertyId);
        when(getMembershipRequestUseCase.getMembershipRequest(any())).thenReturn(view);

        mockMvc.perform(patch("/api/v1/membership-requests/" + view.id() + "/accept")
                        .header("Authorization", bearerToken("ROLE_USER")))
                .andExpect(status().isForbidden());
    }
}
