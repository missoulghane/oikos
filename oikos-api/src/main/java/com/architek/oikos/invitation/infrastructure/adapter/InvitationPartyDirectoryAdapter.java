package com.architek.oikos.invitation.infrastructure.adapter;

import java.util.Optional;

import org.springframework.stereotype.Component;

import com.architek.oikos.invitation.application.port.out.PartyContactInfo;
import com.architek.oikos.invitation.application.port.out.PartyDetails;
import com.architek.oikos.invitation.application.port.out.PartyDirectoryPort;
import com.architek.oikos.party.application.command.CreatePartyCommand;
import com.architek.oikos.party.application.dto.PartyView;
import com.architek.oikos.party.application.port.in.CreatePartyUseCase;
import com.architek.oikos.party.application.port.in.GetPartyUseCase;
import com.architek.oikos.party.application.port.in.LoadPartyIdByEmailUseCase;
import com.architek.oikos.party.application.query.GetPartyQuery;
import com.architek.oikos.party.domain.exception.PartyNotFoundException;
import com.architek.oikos.party.domain.valueobject.PartyId;
import com.architek.oikos.shared.domain.valueobject.EmailVO;
import com.architek.oikos.shared.domain.valueobject.EntityId;

/**
 * Cross-feature adapter: delegates to party's public port-in use cases
 * (CreatePartyUseCase, LoadPartyIdByEmailUseCase, GetPartyUseCase), never to
 * party's repository directly (rule 6).
 */
@Component
public class InvitationPartyDirectoryAdapter implements PartyDirectoryPort {

    private final CreatePartyUseCase createPartyUseCase;
    private final LoadPartyIdByEmailUseCase loadPartyIdByEmailUseCase;
    private final GetPartyUseCase getPartyUseCase;

    public InvitationPartyDirectoryAdapter(CreatePartyUseCase createPartyUseCase,
                                            LoadPartyIdByEmailUseCase loadPartyIdByEmailUseCase,
                                            GetPartyUseCase getPartyUseCase) {
        this.createPartyUseCase = createPartyUseCase;
        this.loadPartyIdByEmailUseCase = loadPartyIdByEmailUseCase;
        this.getPartyUseCase = getPartyUseCase;
    }

    @Override
    public EntityId createParty(PartyDetails details, EntityId propertyId) {
        PartyId id = createPartyUseCase.create(new CreatePartyCommand(
                propertyId, details.fullName(), details.partyType(), details.email(), details.phone(), false));
        return EntityId.of(id.asUuid());
    }

    @Override
    public Optional<EntityId> findIdByEmail(EmailVO email, EntityId propertyId) {
        return loadPartyIdByEmailUseCase.loadByEmail(propertyId, email).map(id -> EntityId.of(id.asUuid()));
    }

    @Override
    public Optional<PartyContactInfo> findById(EntityId partyId) {
        try {
            PartyView view = getPartyUseCase.getParty(new GetPartyQuery(PartyId.of(partyId.value())));
            return Optional.of(new PartyContactInfo(view.propertyId(), view.fullName(),
                    view.email() != null ? EmailVO.of(view.email()) : null));
        } catch (PartyNotFoundException e) {
            return Optional.empty();
        }
    }
}
