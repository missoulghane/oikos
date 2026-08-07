package com.architek.oikos.invitation.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.architek.oikos.invitation.application.dto.InvitationPreviewView;
import com.architek.oikos.invitation.application.port.out.PropertyBasicInfo;
import com.architek.oikos.invitation.application.port.out.PropertyDirectoryPort;
import com.architek.oikos.invitation.application.query.GetInvitationByTokenQuery;
import com.architek.oikos.invitation.domain.exception.InvalidInvitationTokenException;
import com.architek.oikos.invitation.domain.model.Invitation;
import com.architek.oikos.invitation.domain.model.InvitationType;
import com.architek.oikos.invitation.domain.repository.InvitationRepository;
import com.architek.oikos.invitation.domain.valueobject.InvitationId;
import com.architek.oikos.shared.domain.valueobject.EmailVO;
import com.architek.oikos.shared.domain.valueobject.EntityId;

@ExtendWith(MockitoExtension.class)
class GetInvitationByTokenServiceTest {

    private static final Clock CLOCK = Clock.fixed(Instant.EPOCH, ZoneOffset.UTC);

    @Mock
    private InvitationRepository invitationRepository;

    @Mock
    private PropertyDirectoryPort propertyDirectoryPort;

    private GetInvitationByTokenService newService() {
        return new GetInvitationByTokenService(invitationRepository, propertyDirectoryPort, CLOCK);
    }

    @Test
    void previewing_an_unknown_token_is_rejected() {
        when(invitationRepository.findByToken("bad-token")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> newService().getPreview(new GetInvitationByTokenQuery("bad-token")))
                .isInstanceOf(InvalidInvitationTokenException.class);
    }

    @Test
    void previewing_a_usable_private_invitation_includes_property_info() {
        EntityId propertyId = EntityId.newId();
        Invitation invitation = Invitation.issue(InvitationId.newId(), propertyId, InvitationType.PRIVATE,
                "PROPERTY_OWNER", EmailVO.of("jane.doe@example.com"), "tok", CLOCK.instant().plus(Duration.ofDays(1)),
                EntityId.newId(), null);
        when(invitationRepository.findByToken("tok")).thenReturn(Optional.of(invitation));
        when(propertyDirectoryPort.findBasicInfo(propertyId))
                .thenReturn(Optional.of(new PropertyBasicInfo("Copro Test", "1 rue de la Paix")));

        InvitationPreviewView preview = newService().getPreview(new GetInvitationByTokenQuery("tok"));

        assertThat(preview.usable()).isTrue();
        assertThat(preview.propertyName()).isEqualTo("Copro Test");
        assertThat(preview.targetEmail()).isEqualTo(EmailVO.of("jane.doe@example.com"));
    }

    @Test
    void previewing_an_expired_invitation_reports_it_as_unusable() {
        EntityId propertyId = EntityId.newId();
        Invitation invitation = Invitation.issue(InvitationId.newId(), propertyId, InvitationType.PUBLIC,
                "PROPERTY_OWNER", null, "tok", CLOCK.instant().minus(Duration.ofDays(1)), EntityId.newId(), null);
        when(invitationRepository.findByToken("tok")).thenReturn(Optional.of(invitation));
        when(propertyDirectoryPort.findBasicInfo(propertyId))
                .thenReturn(Optional.of(new PropertyBasicInfo("Copro Test", "1 rue de la Paix")));

        InvitationPreviewView preview = newService().getPreview(new GetInvitationByTokenQuery("tok"));

        assertThat(preview.usable()).isFalse();
        assertThat(preview.reason()).isEqualTo("EXPIRED");
    }
}
