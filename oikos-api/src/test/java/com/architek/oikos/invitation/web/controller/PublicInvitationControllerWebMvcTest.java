package com.architek.oikos.invitation.web.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.architek.oikos.auth.infrastructure.security.JwtService;
import com.architek.oikos.invitation.application.command.AcceptInvitationCommand;
import com.architek.oikos.invitation.application.command.SubmitMembershipRequestCommand;
import com.architek.oikos.invitation.application.dto.InvitationPreviewView;
import com.architek.oikos.invitation.application.port.in.AcceptInvitationUseCase;
import com.architek.oikos.invitation.application.port.in.GetInvitationByTokenUseCase;
import com.architek.oikos.invitation.application.port.in.ListAvailableUnitsForInvitationUseCase;
import com.architek.oikos.invitation.application.port.in.SubmitMembershipRequestUseCase;
import com.architek.oikos.invitation.application.port.out.AvailableUnitInfo;
import com.architek.oikos.invitation.domain.model.InvitationType;
import com.architek.oikos.shared.domain.pagination.Page;
import com.architek.oikos.shared.domain.valueobject.EntityId;
import com.architek.oikos.testsupport.WebSecuritySliceTestConfiguration;

import java.util.List;

@WebMvcTest(controllers = PublicInvitationController.class)
@Import(WebSecuritySliceTestConfiguration.class)
class PublicInvitationControllerWebMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtService jwtService;

    @MockitoBean
    private GetInvitationByTokenUseCase getInvitationByTokenUseCase;

    @MockitoBean
    private ListAvailableUnitsForInvitationUseCase listAvailableUnitsForInvitationUseCase;

    @MockitoBean
    private AcceptInvitationUseCase acceptInvitationUseCase;

    @MockitoBean
    private SubmitMembershipRequestUseCase submitMembershipRequestUseCase;

    private String bearerToken(String... authorities) {
        return "Bearer " + jwtService.generateAccessToken(EntityId.of(UUID.randomUUID()), Set.of(authorities));
    }

    @Test
    void preview_is_reachable_without_authentication() throws Exception {
        when(getInvitationByTokenUseCase.getPreview(any())).thenReturn(
                new InvitationPreviewView(InvitationType.PUBLIC, true, null, "Copro Test", "1 rue de la Paix",
                        null, null, null));

        mockMvc.perform(get("/api/v1/invitations/by-token/tok")).andExpect(status().isOk());
    }

    @Test
    void available_units_is_reachable_without_authentication() throws Exception {
        when(listAvailableUnitsForInvitationUseCase.list(any())).thenReturn(
                Page.of(List.of(new AvailableUnitInfo(EntityId.newId(), "A1", "Appartement")), 0, 20, 1));

        mockMvc.perform(get("/api/v1/invitations/by-token/tok/available-units")).andExpect(status().isOk());
    }

    @Test
    void accepting_anonymously_carries_the_request_body_fields_and_no_acting_user() throws Exception {
        mockMvc.perform(post("/api/v1/invitations/by-token/tok/accept")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"jane.doe@example.com","fullName":"Jane Doe","password":"password123"}
                                """))
                .andExpect(status().isNoContent());

        ArgumentCaptor<AcceptInvitationCommand> captor = ArgumentCaptor.forClass(AcceptInvitationCommand.class);
        verify(acceptInvitationUseCase).accept(captor.capture());
        AcceptInvitationCommand command = captor.getValue();
        org.assertj.core.api.Assertions.assertThat(command.actingUserId()).isNull();
        org.assertj.core.api.Assertions.assertThat(command.email().value()).isEqualTo("jane.doe@example.com");
    }

    @Test
    void accepting_with_a_valid_bearer_token_carries_the_acting_user_id_and_ignores_the_body() throws Exception {
        mockMvc.perform(post("/api/v1/invitations/by-token/tok/accept")
                        .header("Authorization", bearerToken("ROLE_USER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isNoContent());

        ArgumentCaptor<AcceptInvitationCommand> captor = ArgumentCaptor.forClass(AcceptInvitationCommand.class);
        verify(acceptInvitationUseCase).accept(captor.capture());
        AcceptInvitationCommand command = captor.getValue();
        org.assertj.core.api.Assertions.assertThat(command.actingUserId()).isNotNull();
        org.assertj.core.api.Assertions.assertThat(command.email()).isNull();
    }

    @Test
    void submitting_a_candidacy_anonymously_carries_the_request_body_fields_and_no_acting_user() throws Exception {
        mockMvc.perform(post("/api/v1/invitations/by-token/tok/candidacies")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"jane.doe@example.com","fullName":"Jane Doe","password":"password123","unitId":"%s"}
                                """.formatted(UUID.randomUUID())))
                .andExpect(status().isNoContent());

        ArgumentCaptor<SubmitMembershipRequestCommand> captor = ArgumentCaptor.forClass(SubmitMembershipRequestCommand.class);
        verify(submitMembershipRequestUseCase).submit(captor.capture());
        SubmitMembershipRequestCommand command = captor.getValue();
        org.assertj.core.api.Assertions.assertThat(command.actingUserId()).isNull();
        org.assertj.core.api.Assertions.assertThat(command.email().value()).isEqualTo("jane.doe@example.com");
    }
}
