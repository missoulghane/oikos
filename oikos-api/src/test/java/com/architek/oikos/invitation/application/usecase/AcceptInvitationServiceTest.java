package com.architek.oikos.invitation.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
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

import com.architek.oikos.invitation.application.command.AcceptInvitationCommand;
import com.architek.oikos.invitation.application.port.out.AccountDirectoryPort;
import com.architek.oikos.invitation.application.port.out.AccountInfo;
import com.architek.oikos.invitation.application.port.out.PartyDirectoryPort;
import com.architek.oikos.invitation.application.port.out.UnitBasicInfo;
import com.architek.oikos.invitation.application.port.out.UnitDirectoryPort;
import com.architek.oikos.invitation.domain.exception.InvalidInvitationTokenException;
import com.architek.oikos.invitation.domain.exception.UnitUnavailableException;
import com.architek.oikos.invitation.domain.model.Invitation;
import com.architek.oikos.invitation.domain.model.InvitationStatus;
import com.architek.oikos.invitation.domain.model.InvitationType;
import com.architek.oikos.invitation.domain.repository.InvitationRepository;
import com.architek.oikos.invitation.domain.valueobject.InvitationId;
import com.architek.oikos.shared.domain.valueobject.EmailVO;
import com.architek.oikos.shared.domain.valueobject.EntityId;
import com.architek.oikos.shared.domain.valueobject.RawPassword;

@ExtendWith(MockitoExtension.class)
class AcceptInvitationServiceTest {

    private static final Clock CLOCK = Clock.fixed(Instant.EPOCH, ZoneOffset.UTC);

    @Mock
    private InvitationRepository invitationRepository;

    @Mock
    private PartyDirectoryPort partyDirectoryPort;

    @Mock
    private UnitDirectoryPort unitDirectoryPort;

    @Mock
    private AccountDirectoryPort accountDirectoryPort;

    private AcceptInvitationService newService() {
        return new AcceptInvitationService(invitationRepository, partyDirectoryPort, unitDirectoryPort, accountDirectoryPort, CLOCK);
    }

    private Invitation withUnitInvitation(EntityId propertyId, EntityId unitId) {
        return Invitation.issue(InvitationId.newId(), propertyId, InvitationType.PRIVATE_WITH_UNIT, "PROPERTY_OWNER",
                unitId, EmailVO.of("jane.doe@example.com"), "tok", CLOCK.instant().plus(Duration.ofDays(1)), EntityId.newId());
    }

    @Test
    void accepting_anonymously_provisions_an_account_creates_a_party_claims_the_unit_and_grants_the_role() {
        EntityId propertyId = EntityId.newId();
        EntityId unitId = EntityId.newId();
        Invitation invitation = withUnitInvitation(propertyId, unitId);
        when(invitationRepository.findByToken("tok")).thenReturn(Optional.of(invitation));
        when(invitationRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        EntityId newUserId = EntityId.newId();
        EntityId newPartyId = EntityId.newId();
        when(accountDirectoryPort.provisionAccount(eq(EmailVO.of("jane.doe@example.com")), eq("Jane Doe"), any()))
                .thenReturn(newUserId);
        when(partyDirectoryPort.findIdByEmail(EmailVO.of("jane.doe@example.com"), propertyId)).thenReturn(Optional.empty());
        when(partyDirectoryPort.createParty(any(), eq(propertyId))).thenReturn(newPartyId);

        EntityId result = newService().accept(new AcceptInvitationCommand("tok", null,
                EmailVO.of("jane.doe@example.com"), "Jane Doe", RawPassword.of("password123"), null));

        assertThat(result).isEqualTo(newUserId);
        verify(unitDirectoryPort).claim(unitId, newPartyId);
        verify(accountDirectoryPort).grantPropertyRole(newUserId, newPartyId, propertyId, "PROPERTY_OWNER");
        ArgumentCaptor<Invitation> captor = ArgumentCaptor.forClass(Invitation.class);
        verify(invitationRepository, times(1)).save(captor.capture());
        assertThat(captor.getValue().getStatus()).isEqualTo(InvitationStatus.CONSUMED);
    }

    @Test
    void accepting_with_an_existing_session_reuses_the_caller_s_own_account_and_party() {
        EntityId propertyId = EntityId.newId();
        EntityId unitId = EntityId.newId();
        Invitation invitation = withUnitInvitation(propertyId, unitId);
        when(invitationRepository.findByToken("tok")).thenReturn(Optional.of(invitation));
        when(invitationRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        EntityId actingUserId = EntityId.newId();
        EntityId existingPartyId = EntityId.newId();
        when(accountDirectoryPort.getAccountInfo(actingUserId))
                .thenReturn(new AccountInfo(EmailVO.of("existing@example.com"), "Existing User"));
        when(partyDirectoryPort.findIdByEmail(EmailVO.of("existing@example.com"), propertyId))
                .thenReturn(Optional.of(existingPartyId));

        EntityId result = newService().accept(new AcceptInvitationCommand("tok", actingUserId, null, null, null, null));

        assertThat(result).isEqualTo(actingUserId);
        verify(accountDirectoryPort, never()).provisionAccount(any(), any(), any());
        verify(partyDirectoryPort, never()).createParty(any(), any());
        verify(unitDirectoryPort).claim(unitId, existingPartyId);
    }

    @Test
    void accepting_an_invalid_token_is_rejected() {
        when(invitationRepository.findByToken("bad")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> newService().accept(new AcceptInvitationCommand("bad", null,
                EmailVO.of("jane.doe@example.com"), "Jane Doe", RawPassword.of("password123"), null)))
                .isInstanceOf(InvalidInvitationTokenException.class);
    }

    @Test
    void accepting_a_public_invitation_through_this_use_case_is_rejected() {
        Invitation invitation = Invitation.issue(InvitationId.newId(), EntityId.newId(), InvitationType.PUBLIC,
                "PROPERTY_OWNER", null, null, "tok", CLOCK.instant().plus(Duration.ofDays(1)), EntityId.newId());
        when(invitationRepository.findByToken("tok")).thenReturn(Optional.of(invitation));

        assertThatThrownBy(() -> newService().accept(new AcceptInvitationCommand("tok", null,
                EmailVO.of("jane.doe@example.com"), "Jane Doe", RawPassword.of("password123"), null)))
                .isInstanceOf(InvalidInvitationTokenException.class);
    }

    @Test
    void losing_the_claim_race_disables_the_invitation_and_does_not_consume_it() {
        EntityId propertyId = EntityId.newId();
        EntityId unitId = EntityId.newId();
        Invitation invitation = withUnitInvitation(propertyId, unitId);
        when(invitationRepository.findByToken("tok")).thenReturn(Optional.of(invitation));
        when(invitationRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        EntityId newUserId = EntityId.newId();
        EntityId newPartyId = EntityId.newId();
        when(accountDirectoryPort.provisionAccount(any(), any(), any())).thenReturn(newUserId);
        when(partyDirectoryPort.findIdByEmail(any(), eq(propertyId))).thenReturn(Optional.empty());
        when(partyDirectoryPort.createParty(any(), eq(propertyId))).thenReturn(newPartyId);
        org.mockito.Mockito.doThrow(new UnitUnavailableException("already claimed"))
                .when(unitDirectoryPort).claim(unitId, newPartyId);

        assertThatThrownBy(() -> newService().accept(new AcceptInvitationCommand("tok", null,
                EmailVO.of("jane.doe@example.com"), "Jane Doe", RawPassword.of("password123"), null)))
                .isInstanceOf(UnitUnavailableException.class);

        ArgumentCaptor<Invitation> captor = ArgumentCaptor.forClass(Invitation.class);
        verify(invitationRepository).save(captor.capture());
        assertThat(captor.getValue().getStatus()).isEqualTo(InvitationStatus.DISABLED);
        verify(accountDirectoryPort, never()).grantPropertyRole(any(), any(), any(), any());
    }

    @Test
    void accepting_a_without_unit_invitation_claims_the_client_supplied_unit() {
        EntityId propertyId = EntityId.newId();
        EntityId chosenUnitId = EntityId.newId();
        Invitation invitation = Invitation.issue(InvitationId.newId(), propertyId, InvitationType.PRIVATE_WITHOUT_UNIT,
                "PROPERTY_OWNER", null, EmailVO.of("jane.doe@example.com"), "tok", CLOCK.instant().plus(Duration.ofDays(1)),
                EntityId.newId());
        when(invitationRepository.findByToken("tok")).thenReturn(Optional.of(invitation));
        when(invitationRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(unitDirectoryPort.findBasicInfo(chosenUnitId))
                .thenReturn(Optional.of(new UnitBasicInfo(propertyId, "A1", "Appartement", true)));

        EntityId newUserId = EntityId.newId();
        EntityId newPartyId = EntityId.newId();
        when(accountDirectoryPort.provisionAccount(any(), any(), any())).thenReturn(newUserId);
        when(partyDirectoryPort.findIdByEmail(any(), eq(propertyId))).thenReturn(Optional.empty());
        when(partyDirectoryPort.createParty(any(), eq(propertyId))).thenReturn(newPartyId);

        newService().accept(new AcceptInvitationCommand("tok", null, EmailVO.of("jane.doe@example.com"), "Jane Doe",
                RawPassword.of("password123"), chosenUnitId));

        verify(unitDirectoryPort).claim(chosenUnitId, newPartyId);
    }

    @Test
    void accepting_a_without_unit_invitation_without_a_unit_id_is_rejected() {
        Invitation invitation = Invitation.issue(InvitationId.newId(), EntityId.newId(), InvitationType.PRIVATE_WITHOUT_UNIT,
                "PROPERTY_OWNER", null, EmailVO.of("jane.doe@example.com"), "tok", CLOCK.instant().plus(Duration.ofDays(1)),
                EntityId.newId());
        when(invitationRepository.findByToken("tok")).thenReturn(Optional.of(invitation));

        assertThatThrownBy(() -> newService().accept(new AcceptInvitationCommand("tok", null,
                EmailVO.of("jane.doe@example.com"), "Jane Doe", RawPassword.of("password123"), null)))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void accepting_a_without_unit_invitation_with_a_unit_from_another_property_is_rejected() {
        EntityId propertyId = EntityId.newId();
        EntityId otherPropertyId = EntityId.newId();
        EntityId chosenUnitId = EntityId.newId();
        Invitation invitation = Invitation.issue(InvitationId.newId(), propertyId, InvitationType.PRIVATE_WITHOUT_UNIT,
                "PROPERTY_OWNER", null, EmailVO.of("jane.doe@example.com"), "tok", CLOCK.instant().plus(Duration.ofDays(1)),
                EntityId.newId());
        when(invitationRepository.findByToken("tok")).thenReturn(Optional.of(invitation));
        when(unitDirectoryPort.findBasicInfo(chosenUnitId))
                .thenReturn(Optional.of(new UnitBasicInfo(otherPropertyId, "A1", "Appartement", true)));

        assertThatThrownBy(() -> newService().accept(new AcceptInvitationCommand("tok", null,
                EmailVO.of("jane.doe@example.com"), "Jane Doe", RawPassword.of("password123"), chosenUnitId)))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
