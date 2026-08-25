package com.architek.oikos.invitation.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.architek.oikos.invitation.application.dto.InvitationView;
import com.architek.oikos.invitation.application.query.GetInvitationQuery;
import com.architek.oikos.invitation.domain.exception.InvitationNotFoundException;
import com.architek.oikos.invitation.domain.model.Invitation;
import com.architek.oikos.invitation.domain.model.InvitationType;
import com.architek.oikos.invitation.domain.repository.InvitationRepository;
import com.architek.oikos.invitation.domain.valueobject.InvitationId;
import com.architek.oikos.shared.domain.valueobject.EntityId;

@ExtendWith(MockitoExtension.class)
class GetInvitationServiceTest {

    @Mock
    private InvitationRepository invitationRepository;

    @Mock
    private InvitationLinkComposer linkComposer;

    private GetInvitationService newService() {
        return new GetInvitationService(invitationRepository, linkComposer);
    }

    @Test
    void getting_a_known_invitation_returns_its_view_with_a_link() {
        InvitationId id = InvitationId.newId();
        Invitation invitation = Invitation.issue(id, EntityId.newId(), InvitationType.PUBLIC, "PROPERTY_OWNER", null,
                "tok", Instant.EPOCH.plus(Duration.ofDays(30)), EntityId.newId(), null, null, null);
        when(invitationRepository.findById(id)).thenReturn(Optional.of(invitation));
        when(linkComposer.link("tok")).thenReturn("http://localhost/invitations?token=tok");

        InvitationView view = newService().getInvitation(new GetInvitationQuery(id));

        assertThat(view.link()).isEqualTo("http://localhost/invitations?token=tok");
    }

    @Test
    void getting_an_unknown_invitation_is_rejected() {
        InvitationId id = InvitationId.newId();
        when(invitationRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> newService().getInvitation(new GetInvitationQuery(id)))
                .isInstanceOf(InvitationNotFoundException.class);
    }
}
