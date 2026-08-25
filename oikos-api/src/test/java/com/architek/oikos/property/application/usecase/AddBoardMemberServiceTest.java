package com.architek.oikos.property.application.usecase;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.architek.oikos.property.application.command.AddBoardMemberCommand;
import com.architek.oikos.property.application.port.out.AccountDirectoryPort;
import com.architek.oikos.property.application.port.out.PartyDetails;
import com.architek.oikos.property.application.port.out.PartyDirectoryPort;
import com.architek.oikos.property.domain.exception.PartyAlreadyHasRoleException;
import com.architek.oikos.property.domain.exception.PropertyNotFoundException;
import com.architek.oikos.property.domain.model.Property;
import com.architek.oikos.property.domain.repository.PropertyRepository;
import com.architek.oikos.property.domain.repository.BoardMemberRepository;
import com.architek.oikos.property.domain.valueobject.PropertyId;
import com.architek.oikos.property.domain.valueobject.BoardRole;
import com.architek.oikos.shared.domain.valueobject.EmailVO;
import com.architek.oikos.shared.domain.valueobject.EntityId;
import com.architek.oikos.shared.domain.valueobject.PartyType;

@ExtendWith(MockitoExtension.class)
class AddBoardMemberServiceTest {

    @Mock
    private BoardMemberRepository boardMemberRepository;

    @Mock
    private PropertyRepository propertyRepository;

    @Mock
    private PartyDirectoryPort partyDirectoryPort;

    @Mock
    private AccountDirectoryPort accountDirectoryPort;

    private AddBoardMemberService newService() {
        return new AddBoardMemberService(boardMemberRepository, propertyRepository, partyDirectoryPort,
                accountDirectoryPort);
    }

    @Test
    void adding_a_board_member_to_an_existing_property_persists_it() {
        PropertyId propertyId = PropertyId.newId();
        when(propertyRepository.findById(propertyId))
                .thenReturn(Optional.of(Property.create(propertyId, "Copro", "Address")));
        when(boardMemberRepository.existsByPropertyIdAndPartyIdAndBoardRole(any(), any(), any())).thenReturn(false);
        when(boardMemberRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        newService().add(new AddBoardMemberCommand(propertyId, EntityId.newId(), null, null, null, BoardRole.PRESIDENT));
    }

    @Test
    void adding_the_same_role_twice_for_the_same_party_and_property_is_rejected() {
        PropertyId propertyId = PropertyId.newId();
        when(propertyRepository.findById(propertyId))
                .thenReturn(Optional.of(Property.create(propertyId, "Copro", "Address")));
        when(boardMemberRepository.existsByPropertyIdAndPartyIdAndBoardRole(any(), any(), any())).thenReturn(true);

        assertThatThrownBy(() -> newService().add(
                new AddBoardMemberCommand(propertyId, EntityId.newId(), null, null, null, BoardRole.PRESIDENT)))
                .isInstanceOf(PartyAlreadyHasRoleException.class);
    }

    @Test
    void adding_a_board_member_to_an_unknown_property_is_rejected() {
        PropertyId propertyId = PropertyId.newId();
        when(propertyRepository.findById(propertyId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> newService().add(
                new AddBoardMemberCommand(propertyId, EntityId.newId(), null, null, null, BoardRole.PRESIDENT)))
                .isInstanceOf(PropertyNotFoundException.class);
    }

    @Test
    void adding_a_board_member_without_a_party_id_creates_the_party_inline() {
        PropertyId propertyId = PropertyId.newId();
        EntityId newPartyId = EntityId.newId();
        when(propertyRepository.findById(propertyId))
                .thenReturn(Optional.of(Property.create(propertyId, "Copro", "Address")));
        when(partyDirectoryPort.createParty(any(), eq(propertyId.value()))).thenReturn(newPartyId);
        when(boardMemberRepository.existsByPropertyIdAndPartyIdAndBoardRole(propertyId, newPartyId, BoardRole.TREASURER))
                .thenReturn(false);
        when(boardMemberRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        newService().add(new AddBoardMemberCommand(propertyId, null, "Jane Doe", null, "0600000000", BoardRole.TREASURER));

        verify(partyDirectoryPort).createParty(
                new PartyDetails("Jane Doe", PartyType.INDIVIDUAL, null, "0600000000"), propertyId.value());
        verify(boardMemberRepository).save(any());
    }

    @Test
    void adding_a_board_member_without_a_party_id_reuses_an_existing_party_matching_the_email() {
        PropertyId propertyId = PropertyId.newId();
        EntityId existingPartyId = EntityId.newId();
        when(propertyRepository.findById(propertyId))
                .thenReturn(Optional.of(Property.create(propertyId, "Copro", "Address")));
        when(partyDirectoryPort.findIdByEmail(EmailVO.of("jane.doe@example.com"), propertyId.value()))
                .thenReturn(Optional.of(existingPartyId));
        when(boardMemberRepository.existsByPropertyIdAndPartyIdAndBoardRole(propertyId, existingPartyId, BoardRole.SECRETARY))
                .thenReturn(false);
        when(boardMemberRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        newService().add(new AddBoardMemberCommand(propertyId, null, "Jane Doe", "jane.doe@example.com", null,
                BoardRole.SECRETARY));

        verify(partyDirectoryPort, never()).createParty(any(), any());
    }

    @Test
    void adding_a_board_member_without_a_party_id_nor_a_full_name_is_rejected() {
        PropertyId propertyId = PropertyId.newId();
        when(propertyRepository.findById(propertyId))
                .thenReturn(Optional.of(Property.create(propertyId, "Copro", "Address")));

        assertThatThrownBy(() -> newService().add(
                new AddBoardMemberCommand(propertyId, null, null, null, null, BoardRole.PRESIDENT)))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void a_member_known_only_by_phone_is_not_duplicated() {
        // Le trou que le rattachement d'un lot venait de refermer : sans ce second
        // filet, la copropriété se retrouvait avec deux fiches pour une personne
        // déjà enregistrée, et deux convocations à la prochaine AG.
        PropertyId propertyId = PropertyId.newId();
        EntityId existingPartyId = EntityId.newId();
        when(propertyRepository.findById(propertyId))
                .thenReturn(Optional.of(Property.create(propertyId, "Copro", "Address")));
        when(partyDirectoryPort.findIdByPhone(eq("+212612345678"), any())).thenReturn(Optional.of(existingPartyId));
        when(boardMemberRepository.existsByPropertyIdAndPartyIdAndBoardRole(any(), any(), any())).thenReturn(false);
        when(boardMemberRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        newService().add(new AddBoardMemberCommand(propertyId, null, "Jane Doe", null, "+212612345678",
                BoardRole.TREASURER));

        verify(partyDirectoryPort, never()).createParty(any(), any());
    }

    @Test
    void a_member_without_an_email_is_recorded_instead_of_crashing() {
        // Le formulaire annonce « Email (optionnel) » : la fiche se crée sans
        // adresse, là où Party la refusait et l'appel remontait en 500.
        PropertyId propertyId = PropertyId.newId();
        EntityId newPartyId = EntityId.newId();
        when(propertyRepository.findById(propertyId))
                .thenReturn(Optional.of(Property.create(propertyId, "Copro", "Address")));
        when(partyDirectoryPort.findIdByPhone(any(), any())).thenReturn(Optional.empty());
        when(partyDirectoryPort.createParty(any(), any())).thenReturn(newPartyId);
        when(boardMemberRepository.existsByPropertyIdAndPartyIdAndBoardRole(any(), any(), any())).thenReturn(false);
        when(boardMemberRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        newService().add(new AddBoardMemberCommand(propertyId, null, "Jane Doe", null, "+212612345678",
                BoardRole.TREASURER));

        verify(partyDirectoryPort).createParty(
                new PartyDetails("Jane Doe", PartyType.INDIVIDUAL, null, "+212612345678"), propertyId.value());
    }

    /**
     * Le cas le plus trompeur : le syndic tape l'adresse avec laquelle la
     * personne se connecte, qui n'est pas celle inscrite sur sa fiche. Rien ne
     * correspondait, et un second contact naissait pour quelqu'un qui possède
     * déjà un lot dans l'immeuble - avec le nom, parfois approximatif, saisi à
     * ce moment-là.
     */
    @Test
    void a_contact_reached_only_through_its_account_email_is_reused_rather_than_duplicated() {
        PropertyId propertyId = PropertyId.newId();
        EntityId existingPartyId = EntityId.newId();
        when(propertyRepository.findById(propertyId))
                .thenReturn(Optional.of(Property.create(propertyId, "Copro", "Address")));
        when(partyDirectoryPort.findIdByEmail(EmailVO.of("user1@oikos.com"), propertyId.value()))
                .thenReturn(Optional.empty());
        when(partyDirectoryPort.findIdByPhone(any(), any())).thenReturn(Optional.empty());
        when(accountDirectoryPort.findLinkedPartyInProperty(EmailVO.of("user1@oikos.com"), propertyId.value()))
                .thenReturn(Optional.of(existingPartyId));
        when(boardMemberRepository.existsByPropertyIdAndPartyIdAndBoardRole(any(), any(), any())).thenReturn(false);
        when(boardMemberRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        newService().add(new AddBoardMemberCommand(propertyId, null, "Nom saisi n'importe comment",
                "user1@oikos.com", null, BoardRole.TREASURER));

        verify(partyDirectoryPort, never()).createParty(any(), any());
    }

    /** Personne derrière cette adresse : la fiche se crée, comme avant. */
    @Test
    void an_unknown_email_still_creates_the_contact() {
        PropertyId propertyId = PropertyId.newId();
        EntityId createdPartyId = EntityId.newId();
        when(propertyRepository.findById(propertyId))
                .thenReturn(Optional.of(Property.create(propertyId, "Copro", "Address")));
        when(partyDirectoryPort.findIdByEmail(any(), any())).thenReturn(Optional.empty());
        when(partyDirectoryPort.findIdByPhone(any(), any())).thenReturn(Optional.empty());
        when(accountDirectoryPort.findLinkedPartyInProperty(any(), any())).thenReturn(Optional.empty());
        when(partyDirectoryPort.createParty(any(), eq(propertyId.value()))).thenReturn(createdPartyId);
        when(boardMemberRepository.existsByPropertyIdAndPartyIdAndBoardRole(any(), any(), any())).thenReturn(false);
        when(boardMemberRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        newService().add(new AddBoardMemberCommand(propertyId, null, "Karim Alami", "inconnu@oikos.com", null,
                BoardRole.MEMBER));

        verify(partyDirectoryPort).createParty(any(PartyDetails.class), eq(propertyId.value()));
    }
}