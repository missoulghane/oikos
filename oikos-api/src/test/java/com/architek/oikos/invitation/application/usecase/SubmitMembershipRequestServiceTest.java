package com.architek.oikos.invitation.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
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

import com.architek.oikos.invitation.application.command.SubmitMembershipRequestCommand;
import com.architek.oikos.invitation.application.port.out.AccountDirectoryPort;
import com.architek.oikos.invitation.application.port.out.PartyDirectoryPort;
import com.architek.oikos.invitation.application.port.out.UnitBasicInfo;
import com.architek.oikos.invitation.application.port.out.UnitDirectoryPort;
import com.architek.oikos.invitation.domain.exception.InvalidInvitationTokenException;
import com.architek.oikos.invitation.domain.model.Invitation;
import com.architek.oikos.invitation.domain.model.InvitationType;
import com.architek.oikos.invitation.domain.model.MembershipRequest;
import com.architek.oikos.invitation.domain.model.MembershipRequestStatus;
import com.architek.oikos.invitation.domain.repository.InvitationRepository;
import com.architek.oikos.invitation.domain.repository.MembershipRequestRepository;
import com.architek.oikos.invitation.domain.valueobject.InvitationId;
import com.architek.oikos.shared.domain.valueobject.EmailVO;
import com.architek.oikos.shared.domain.valueobject.EntityId;
import com.architek.oikos.shared.domain.valueobject.RawPassword;

@ExtendWith(MockitoExtension.class)
class SubmitMembershipRequestServiceTest {

    private static final Clock CLOCK = Clock.fixed(Instant.EPOCH, ZoneOffset.UTC);

    @Mock
    private InvitationRepository invitationRepository;

    @Mock
    private MembershipRequestRepository membershipRequestRepository;

    @Mock
    private PartyDirectoryPort partyDirectoryPort;

    @Mock
    private UnitDirectoryPort unitDirectoryPort;

    @Mock
    private AccountDirectoryPort accountDirectoryPort;

    private SubmitMembershipRequestService newService() {
        return new SubmitMembershipRequestService(invitationRepository, membershipRequestRepository, partyDirectoryPort,
                unitDirectoryPort, accountDirectoryPort, CLOCK);
    }

    private Invitation publicInvitation(EntityId propertyId) {
        return Invitation.issue(InvitationId.newId(), propertyId, InvitationType.PUBLIC, "PROPERTY_OWNER", null, null,
                "tok", CLOCK.instant().plus(Duration.ofDays(30)), EntityId.newId());
    }

    @Test
    void submitting_a_candidacy_anonymously_creates_a_pending_request() {
        EntityId propertyId = EntityId.newId();
        EntityId unitId = EntityId.newId();
        Invitation invitation = publicInvitation(propertyId);
        when(invitationRepository.findByToken("tok")).thenReturn(Optional.of(invitation));
        when(unitDirectoryPort.findBasicInfo(unitId)).thenReturn(Optional.of(new UnitBasicInfo(propertyId, "A1", "Appartement", true)));

        EntityId newUserId = EntityId.newId();
        EntityId newPartyId = EntityId.newId();
        when(accountDirectoryPort.provisionAccount(eq(EmailVO.of("jane.doe@example.com")), eq("Jane Doe"), any()))
                .thenReturn(newUserId);
        when(partyDirectoryPort.findIdByEmail(EmailVO.of("jane.doe@example.com"), propertyId)).thenReturn(Optional.empty());
        when(partyDirectoryPort.createParty(any(), eq(propertyId))).thenReturn(newPartyId);
        when(membershipRequestRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        newService().submit(new SubmitMembershipRequestCommand("tok", null, EmailVO.of("jane.doe@example.com"), "Jane Doe",
                RawPassword.of("password123"), unitId));

        ArgumentCaptor<MembershipRequest> captor = ArgumentCaptor.forClass(MembershipRequest.class);
        org.mockito.Mockito.verify(membershipRequestRepository).save(captor.capture());
        assertThat(captor.getValue().getStatus()).isEqualTo(MembershipRequestStatus.PENDING);
        assertThat(captor.getValue().getUnitId()).isEqualTo(unitId);
        assertThat(captor.getValue().getPartyId()).isEqualTo(newPartyId);
        assertThat(captor.getValue().getUserId()).isEqualTo(newUserId);
    }

    @Test
    void submitting_a_candidacy_for_a_private_invitation_is_rejected() {
        Invitation invitation = Invitation.issue(InvitationId.newId(), EntityId.newId(), InvitationType.PRIVATE_WITH_UNIT,
                "PROPERTY_OWNER", EntityId.newId(), EmailVO.of("jane.doe@example.com"), "tok",
                CLOCK.instant().plus(Duration.ofDays(30)), EntityId.newId());
        when(invitationRepository.findByToken("tok")).thenReturn(Optional.of(invitation));

        assertThatThrownBy(() -> newService().submit(new SubmitMembershipRequestCommand("tok", null,
                EmailVO.of("jane.doe@example.com"), "Jane Doe", RawPassword.of("password123"), EntityId.newId())))
                .isInstanceOf(InvalidInvitationTokenException.class);
    }

    @Test
    void submitting_a_candidacy_without_a_unit_id_is_rejected() {
        Invitation invitation = publicInvitation(EntityId.newId());
        when(invitationRepository.findByToken("tok")).thenReturn(Optional.of(invitation));

        assertThatThrownBy(() -> newService().submit(new SubmitMembershipRequestCommand("tok", null,
                EmailVO.of("jane.doe@example.com"), "Jane Doe", RawPassword.of("password123"), null)))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
