package com.architek.oikos.invitation.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.architek.oikos.invitation.application.command.EnableInvitationCommand;
import com.architek.oikos.invitation.domain.exception.InvitationNotFoundException;
import com.architek.oikos.invitation.domain.model.Invitation;
import com.architek.oikos.invitation.domain.model.InvitationStatus;
import com.architek.oikos.invitation.domain.model.InvitationType;
import com.architek.oikos.invitation.domain.repository.InvitationRepository;
import com.architek.oikos.invitation.domain.valueobject.InvitationId;
import com.architek.oikos.shared.domain.valueobject.EmailVO;
import com.architek.oikos.shared.domain.valueobject.EntityId;

@ExtendWith(MockitoExtension.class)
class EnableInvitationServiceTest {

    private static final Clock CLOCK = Clock.fixed(Instant.EPOCH.plus(Duration.ofDays(400)), ZoneOffset.UTC);

    @Mock
    private InvitationRepository invitationRepository;

    private EnableInvitationService newService() {
        return new EnableInvitationService(invitationRepository, CLOCK, 30L);
    }

    private Invitation disabledPublicLink(InvitationId id, Instant expiresAt) {
        return Invitation.issue(id, EntityId.newId(), InvitationType.PUBLIC, "PROPERTY_OWNER", null, "tok",
                expiresAt, EntityId.newId(), null, null, null).disable();
    }

    /**
     * Rouvert sur son propre jeton : le QR code affiché dans le hall doit
     * continuer de fonctionner après une fermeture temporaire.
     */
    @Test
    void re_enabling_keeps_the_token_and_pushes_the_expiry_back() {
        InvitationId id = InvitationId.newId();
        Invitation disabled = disabledPublicLink(id, Instant.EPOCH.plus(Duration.ofDays(30)));
        when(invitationRepository.findById(id)).thenReturn(Optional.of(disabled));
        when(invitationRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        newService().enable(new EnableInvitationCommand(id));

        ArgumentCaptor<Invitation> captor = ArgumentCaptor.forClass(Invitation.class);
        verify(invitationRepository).save(captor.capture());
        assertThat(captor.getValue().getStatus()).isEqualTo(InvitationStatus.ACTIVE);
        assertThat(captor.getValue().getToken()).isEqualTo("tok");
        // Sans repousser l'échéance, un lien rouvert après un an reviendrait
        // ACTIVE mais expiré, donc inutilisable sans que rien ne le dise.
        assertThat(captor.getValue().isUsable(CLOCK.instant())).isTrue();
        assertThat(captor.getValue().getExpiresAt()).isEqualTo(CLOCK.instant().plus(Duration.ofDays(30)));
    }

    /** Un lien privé déjà consommé rouvert rendrait un lien à usage unique utilisable deux fois. */
    @Test
    void re_enabling_a_consumed_invitation_is_refused() {
        InvitationId id = InvitationId.newId();
        Invitation consumed = Invitation.issue(id, EntityId.newId(), InvitationType.PRIVATE, "PROPERTY_OWNER",
                EmailVO.of("jane.doe@example.com"), "tok", Instant.EPOCH.plus(Duration.ofDays(30)), EntityId.newId(), null, null, null)
                .consume(EmailVO.of("jane.doe@example.com"));
        when(invitationRepository.findById(id)).thenReturn(Optional.of(consumed));

        assertThatThrownBy(() -> newService().enable(new EnableInvitationCommand(id)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void re_enabling_an_unknown_invitation_is_rejected() {
        InvitationId id = InvitationId.newId();
        when(invitationRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> newService().enable(new EnableInvitationCommand(id)))
                .isInstanceOf(InvitationNotFoundException.class);
    }
}
