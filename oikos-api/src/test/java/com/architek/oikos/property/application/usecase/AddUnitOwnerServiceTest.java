package com.architek.oikos.property.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.architek.oikos.property.application.command.AddUnitOwnerCommand;
import com.architek.oikos.property.application.command.AddUnitOwnershipCommand;
import com.architek.oikos.property.application.port.in.AddUnitOwnershipUseCase;
import com.architek.oikos.property.application.port.out.AccountDirectoryPort;
import com.architek.oikos.property.application.port.out.AccountLinkingPort;
import com.architek.oikos.property.application.port.out.PartyDetails;
import com.architek.oikos.property.application.port.out.PartyDirectoryPort;
import com.architek.oikos.property.domain.exception.UnitNotFoundException;
import com.architek.oikos.property.domain.model.Unit;
import com.architek.oikos.property.domain.repository.UnitRepository;
import com.architek.oikos.property.domain.valueobject.BuildingId;
import com.architek.oikos.property.domain.valueobject.PropertyId;
import com.architek.oikos.property.domain.valueobject.Shares;
import com.architek.oikos.property.domain.valueobject.UnitId;
import com.architek.oikos.property.domain.valueobject.UnitOwnershipId;
import com.architek.oikos.property.domain.valueobject.UnitTypeDefinitionId;
import com.architek.oikos.shared.domain.valueobject.EmailVO;
import com.architek.oikos.shared.domain.valueobject.EntityId;
import com.architek.oikos.shared.domain.valueobject.PartyType;

@ExtendWith(MockitoExtension.class)
class AddUnitOwnerServiceTest {

    @Mock
    private UnitRepository unitRepository;

    @Mock
    private PartyDirectoryPort partyDirectoryPort;

    @Mock
    private AddUnitOwnershipUseCase addUnitOwnershipUseCase;

    @Mock
    private AccountLinkingPort accountLinkingPort;

    @Mock
    private AccountDirectoryPort accountDirectoryPort;

    private static final EntityId INVITED_BY = EntityId.newId();

    private AddUnitOwnerService newService() {
        return new AddUnitOwnerService(unitRepository, partyDirectoryPort, addUnitOwnershipUseCase, accountLinkingPort,
                accountDirectoryPort);
    }

    private static Unit existingUnit(UnitId id) {
        return existingUnit(id, PropertyId.newId());
    }

    private static Unit existingUnit(UnitId id, PropertyId propertyId) {
        return Unit.create(id, BuildingId.newId(), propertyId, "A12", UnitTypeDefinitionId.newId(),
                Shares.of(BigDecimal.TEN));
    }

    /** La fiche telle que le module invitation la relira pour y adresser le lien. */
    private void ficheWithEmail(String email) {
        when(partyDirectoryPort.getPartyById(any()))
                .thenReturn(new PartyDetails("Jane Doe", PartyType.INDIVIDUAL, EmailVO.of(email), null));
    }

    @Test
    void adding_an_owner_with_an_existing_email_reuses_the_existing_party() {
        UnitId unitId = UnitId.newId();
        EntityId existingPartyId = EntityId.newId();
        when(unitRepository.findById(unitId)).thenReturn(Optional.of(existingUnit(unitId)));
        when(partyDirectoryPort.findIdByEmail(any(), any())).thenReturn(Optional.of(existingPartyId));
        when(addUnitOwnershipUseCase.add(any())).thenReturn(UnitOwnershipId.newId());
        ficheWithEmail("jane.doe@example.com");

        newService().add(new AddUnitOwnerCommand(unitId, "Jane Doe", PartyType.INDIVIDUAL, EmailVO.of("jane.doe@example.com"),
                null, new BigDecimal("50"), true, INVITED_BY));

        verify(partyDirectoryPort, never()).createParty(any(), any());
        ArgumentCaptor<AddUnitOwnershipCommand> captor = ArgumentCaptor.forClass(AddUnitOwnershipCommand.class);
        verify(addUnitOwnershipUseCase).add(captor.capture());
        assertThat(captor.getValue().partyId()).isEqualTo(existingPartyId);
        assertThat(captor.getValue().ownershipShare()).isEqualByComparingTo("50");
    }

    @Test
    void adding_an_owner_with_an_unknown_email_creates_a_new_party() {
        UnitId unitId = UnitId.newId();
        EntityId newPartyId = EntityId.newId();
        when(unitRepository.findById(unitId)).thenReturn(Optional.of(existingUnit(unitId)));
        when(partyDirectoryPort.findIdByEmail(any(), any())).thenReturn(Optional.empty());
        when(partyDirectoryPort.createParty(any(), any())).thenReturn(newPartyId);
        when(addUnitOwnershipUseCase.add(any())).thenReturn(UnitOwnershipId.newId());
        ficheWithEmail("jane.doe@example.com");

        newService().add(new AddUnitOwnerCommand(unitId, "Jane Doe", PartyType.INDIVIDUAL, EmailVO.of("jane.doe@example.com"),
                null, new BigDecimal("50"), true, INVITED_BY));

        ArgumentCaptor<PartyDetails> partyCaptor = ArgumentCaptor.forClass(PartyDetails.class);
        verify(partyDirectoryPort).createParty(partyCaptor.capture(), any());
        assertThat(partyCaptor.getValue().fullName()).isEqualTo("Jane Doe");
        assertThat(partyCaptor.getValue().partyType()).isEqualTo(PartyType.INDIVIDUAL);

        ArgumentCaptor<AddUnitOwnershipCommand> ownershipCaptor = ArgumentCaptor.forClass(AddUnitOwnershipCommand.class);
        verify(addUnitOwnershipUseCase).add(ownershipCaptor.capture());
        assertThat(ownershipCaptor.getValue().partyId()).isEqualTo(newPartyId);
    }

    @Test
    void adding_an_owner_to_an_unknown_unit_is_rejected_without_touching_the_party_directory() {
        UnitId unitId = UnitId.newId();
        when(unitRepository.findById(unitId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> newService().add(new AddUnitOwnerCommand(unitId, "Jane Doe", PartyType.INDIVIDUAL,
                EmailVO.of("jane.doe@example.com"), null, BigDecimal.TEN, true, INVITED_BY)))
                .isInstanceOf(UnitNotFoundException.class);

        verify(partyDirectoryPort, never()).findIdByEmail(any(), any());
        verify(partyDirectoryPort, never()).createParty(any(), any());
        verify(addUnitOwnershipUseCase, never()).add(any());
    }

    @Test
    void an_unknown_email_but_a_known_phone_still_reuses_the_existing_party() {
        // Le syndic ressaisit la même personne avec une autre adresse : sans ce
        // second filet, la copropriété se retrouve avec deux fiches pour un seul
        // copropriétaire, et deux convocations à la prochaine AG.
        UnitId unitId = UnitId.newId();
        EntityId existingPartyId = EntityId.newId();
        when(unitRepository.findById(unitId)).thenReturn(Optional.of(existingUnit(unitId)));
        when(partyDirectoryPort.findIdByEmail(any(), any())).thenReturn(Optional.empty());
        when(partyDirectoryPort.findIdByPhone(eq("+212612345678"), any())).thenReturn(Optional.of(existingPartyId));
        when(addUnitOwnershipUseCase.add(any())).thenReturn(UnitOwnershipId.newId());
        ficheWithEmail("autre.adresse@example.com");

        newService().add(new AddUnitOwnerCommand(unitId, "Jane Doe", PartyType.INDIVIDUAL,
                EmailVO.of("autre.adresse@example.com"), "+212612345678", new BigDecimal("50"), true, INVITED_BY));

        verify(partyDirectoryPort, never()).createParty(any(), any());
        ArgumentCaptor<AddUnitOwnershipCommand> captor = ArgumentCaptor.forClass(AddUnitOwnershipCommand.class);
        verify(addUnitOwnershipUseCase).add(captor.capture());
        assertThat(captor.getValue().partyId()).isEqualTo(existingPartyId);
    }

    @Test
    void the_invitation_goes_out_for_the_lot_that_was_just_attached() {
        // Le lot voyage avec l'invitation : c'est ce qui en fait l'invitation
        // privée du module invitation - le lien /invitations, l'écran qui
        // annonce le lot, et une demande d'adhésion validée par le syndic -
        // plutôt que l'ancien lien /accept-invitation, qui rattachait le compte
        // sans que personne n'ait rien à valider.
        UnitId unitId = UnitId.newId();
        PropertyId propertyId = PropertyId.newId();
        EntityId partyId = EntityId.newId();
        when(unitRepository.findById(unitId)).thenReturn(Optional.of(existingUnit(unitId, propertyId)));
        when(partyDirectoryPort.findIdByEmail(any(), any())).thenReturn(Optional.of(partyId));
        when(addUnitOwnershipUseCase.add(any())).thenReturn(UnitOwnershipId.newId());
        ficheWithEmail("jane.doe@example.com");

        newService().add(new AddUnitOwnerCommand(unitId, "Jane Doe", PartyType.INDIVIDUAL,
                EmailVO.of("jane.doe@example.com"), null, new BigDecimal("50"), true, INVITED_BY));

        verify(accountLinkingPort).inviteOwnerForUnitIfUnlinked(eq(EntityId.of(propertyId.asUuid())), eq(partyId),
                eq(EntityId.of(unitId.asUuid())), eq(INVITED_BY));
    }

    @Test
    void nothing_is_sent_to_a_fiche_without_an_email() {
        // Fiche retrouvée par le téléphone : le syndic a beau saisir une
        // adresse, l'invitation partirait à celle de la fiche (voir
        // CreateInvitationService) - il n'y en a pas, donc rien ne part, et le
        // lot est rattaché quand même.
        UnitId unitId = UnitId.newId();
        when(unitRepository.findById(unitId)).thenReturn(Optional.of(existingUnit(unitId)));
        when(partyDirectoryPort.findIdByEmail(any(), any())).thenReturn(Optional.empty());
        when(partyDirectoryPort.findIdByPhone(eq("+212612345678"), any())).thenReturn(Optional.of(EntityId.newId()));
        when(addUnitOwnershipUseCase.add(any())).thenReturn(UnitOwnershipId.newId());
        when(partyDirectoryPort.getPartyById(any()))
                .thenReturn(new PartyDetails("Jane Doe", PartyType.INDIVIDUAL, null, "+212612345678"));

        newService().add(new AddUnitOwnerCommand(unitId, "Jane Doe", PartyType.INDIVIDUAL,
                EmailVO.of("jane.doe@example.com"), "+212612345678", new BigDecimal("50"), true, INVITED_BY));

        verify(accountLinkingPort, never()).inviteOwnerForUnitIfUnlinked(any(), any(), any(), any());
        verify(addUnitOwnershipUseCase).add(any());
    }

    @Test
    void nothing_is_sent_when_the_syndic_only_records_who_owns_what() {
        // Case décochée : le rattachement est un acte de gestion, pas une
        // sollicitation. L'invitation partait autrefois sans que personne ne
        // l'ait demandée, et le destinataire la découvrait dans sa boîte.
        UnitId unitId = UnitId.newId();
        when(unitRepository.findById(unitId)).thenReturn(Optional.of(existingUnit(unitId)));
        when(partyDirectoryPort.findIdByEmail(any(), any())).thenReturn(Optional.of(EntityId.newId()));
        when(addUnitOwnershipUseCase.add(any())).thenReturn(UnitOwnershipId.newId());

        newService().add(new AddUnitOwnerCommand(unitId, "Jane Doe", PartyType.INDIVIDUAL,
                EmailVO.of("jane.doe@example.com"), null, new BigDecimal("50"), false, INVITED_BY));

        verify(accountLinkingPort, never()).inviteOwnerForUnitIfUnlinked(any(), any(), any(), any());
    }
}
