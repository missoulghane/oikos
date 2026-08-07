package com.architek.oikos.invitation.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.architek.oikos.invitation.application.command.DisableInvitationCommand;
import com.architek.oikos.invitation.domain.exception.InvitationNotFoundException;
import com.architek.oikos.invitation.domain.model.Invitation;
import com.architek.oikos.invitation.domain.model.InvitationStatus;
import com.architek.oikos.invitation.domain.model.InvitationType;
import com.architek.oikos.invitation.domain.repository.InvitationRepository;
import com.architek.oikos.invitation.domain.valueobject.InvitationId;
import com.architek.oikos.shared.domain.valueobject.EntityId;

@ExtendWith(MockitoExtension.class)
class DisableInvitationServiceTest {

    @Mock
    private InvitationRepository invitationRepository;

    private DisableInvitationService newService() {
        return new DisableInvitationService(invitationRepository);
    }

    @Test
    void disabling_a_known_invitation_persists_the_new_status() {
        InvitationId id = InvitationId.newId();
        Invitation invitation = Invitation.issue(id, EntityId.newId(), InvitationType.PUBLIC, "PROPERTY_OWNER", null,
                "tok", Instant.EPOCH.plus(Duration.ofDays(30)), EntityId.newId(), null);
        when(invitationRepository.findById(id)).thenReturn(Optional.of(invitation));
        when(invitationRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        newService().disable(new DisableInvitationCommand(id));

        ArgumentCaptor<Invitation> captor = ArgumentCaptor.forClass(Invitation.class);
        verify(invitationRepository).save(captor.capture());
        assertThat(captor.getValue().getStatus()).isEqualTo(InvitationStatus.DISABLED);
    }

    @Test
    void disabling_an_unknown_invitation_is_rejected() {
        InvitationId id = InvitationId.newId();
        when(invitationRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> newService().disable(new DisableInvitationCommand(id)))
                .isInstanceOf(InvitationNotFoundException.class);
    }
}
