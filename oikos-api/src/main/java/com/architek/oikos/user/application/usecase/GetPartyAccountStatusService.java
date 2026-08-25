package com.architek.oikos.user.application.usecase;

import java.time.Clock;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.architek.oikos.shared.domain.valueobject.EntityId;
import com.architek.oikos.shared.domain.valueobject.PartyAccountStatus;
import com.architek.oikos.user.application.port.in.GetPartyAccountStatusUseCase;
import com.architek.oikos.user.application.port.out.OutstandingPartyInvitationPort;
import com.architek.oikos.user.domain.repository.PartyInvitationTokenRepository;
import com.architek.oikos.user.domain.repository.UserRepository;

/**
 * Le compte l'emporte sur l'invitation : accepter une invitation supprime son
 * jeton (AcceptPartyInvitationService), mais un contact peut aussi avoir ete
 * rattache autrement (demande d'adhesion), auquel cas un vieux jeton peut
 * trainer - un contact rattache est ACTIVE, jeton ou pas.
 *
 * <p>Deux sources pour INVITED, et non plus une seule. Le jeton d'invitation
 * de compte de ce module n'est plus emis par l'interface : l'invitation part
 * desormais de la fiche du contact, porte sur un lot, et vit dans le module
 * invitation. Les deux sont interrogees parce que des jetons emis avant ce
 * changement peuvent encore courir jusqu'a leur expiration - les ignorer
 * ferait disparaitre un bandeau encore vrai.
 */
@Component
public class GetPartyAccountStatusService implements GetPartyAccountStatusUseCase {

    private final UserRepository userRepository;
    private final PartyInvitationTokenRepository partyInvitationTokenRepository;
    private final OutstandingPartyInvitationPort outstandingPartyInvitationPort;
    private final Clock clock;

    public GetPartyAccountStatusService(UserRepository userRepository,
                                          PartyInvitationTokenRepository partyInvitationTokenRepository,
                                          OutstandingPartyInvitationPort outstandingPartyInvitationPort,
                                          Clock clock) {
        this.userRepository = userRepository;
        this.partyInvitationTokenRepository = partyInvitationTokenRepository;
        this.outstandingPartyInvitationPort = outstandingPartyInvitationPort;
        this.clock = clock;
    }

    @Override
    @Transactional(readOnly = true)
    public PartyAccountStatus statusOf(EntityId partyId) {
        if (userRepository.existsByLinkedPartyId(partyId)) {
            return PartyAccountStatus.ACTIVE;
        }
        if (outstandingPartyInvitationPort.existsFor(partyId)) {
            return PartyAccountStatus.INVITED;
        }
        return partyInvitationTokenRepository.findByPartyId(partyId)
                .filter(token -> !token.isExpired(clock.instant()))
                .map(token -> PartyAccountStatus.INVITED)
                .orElse(PartyAccountStatus.NONE);
    }
}
