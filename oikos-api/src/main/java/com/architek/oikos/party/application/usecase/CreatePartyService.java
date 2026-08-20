package com.architek.oikos.party.application.usecase;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.architek.oikos.party.application.command.CreatePartyCommand;
import com.architek.oikos.party.application.port.in.CreatePartyUseCase;
import com.architek.oikos.party.domain.exception.EmailAlreadyUsedException;
import com.architek.oikos.party.domain.exception.PhoneAlreadyUsedException;
import com.architek.oikos.party.domain.model.Party;
import com.architek.oikos.party.domain.repository.PartyRepository;
import com.architek.oikos.party.application.port.out.AccountInvitationPort;
import com.architek.oikos.party.domain.valueobject.PartyId;
import com.architek.oikos.shared.domain.valueobject.EntityId;

/**
 * Crée un contact, et l'invite à se créer un compte quand l'appelant le demande.
 *
 * <p>L'invitation est un choix explicite ({@link CreatePartyCommand#invite()}) et
 * non un effet de bord de la création : un syndic enregistre aussi des contacts
 * qui n'ont rien à faire dans l'application (un gardien, un prestataire), et leur
 * envoyer un lien de création de compte sans l'avoir demandé se voit dans leur
 * boîte, pas dans la nôtre.
 */
@Component
public class CreatePartyService implements CreatePartyUseCase {

    private final PartyRepository partyRepository;
    private final AccountInvitationPort accountInvitationPort;

    public CreatePartyService(PartyRepository partyRepository, AccountInvitationPort accountInvitationPort) {
        this.partyRepository = partyRepository;
        this.accountInvitationPort = accountInvitationPort;
    }

    @Override
    @Transactional
    public PartyId create(CreatePartyCommand command) {
        if (command.email() != null && partyRepository.existsByPropertyIdAndEmail(command.propertyId(), command.email())) {
            throw new EmailAlreadyUsedException(command.email().value());
        }
        if (command.phone() != null && !command.phone().isBlank()
                && partyRepository.existsByPropertyIdAndPhone(command.propertyId(), command.phone())) {
            throw new PhoneAlreadyUsedException(command.phone());
        }
        Party party = Party.create(PartyId.newId(), command.propertyId(), command.fullName(), command.partyType(),
                command.email(), command.phone());
        PartyId id = partyRepository.save(party).getId();
        // Pas d'adresse, pas d'invitation : le lien de création de compte part par
        // email et n'a nulle part où aller. Le contact existe quand même.
        if (command.invite() && command.email() != null) {
            accountInvitationPort.inviteIfUnlinked(EntityId.of(id.asUuid()), command.email(), command.fullName());
        }
        return id;
    }
}
