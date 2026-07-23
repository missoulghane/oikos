package com.architek.oikos.property.infrastructure.adapter;

import java.util.Optional;

import org.springframework.stereotype.Component;

import com.architek.oikos.party.application.command.CreatePartyCommand;
import com.architek.oikos.party.application.dto.PartyView;
import com.architek.oikos.party.application.port.in.CreatePartyUseCase;
import com.architek.oikos.party.application.port.in.GetPartyUseCase;
import com.architek.oikos.party.application.port.in.LoadPartyIdByEmailUseCase;
import com.architek.oikos.party.application.query.GetPartyQuery;
import com.architek.oikos.party.domain.valueobject.PartyId;
import com.architek.oikos.property.application.port.out.PartyDetails;
import com.architek.oikos.property.application.port.out.PartyDirectoryPort;
import com.architek.oikos.shared.domain.valueobject.EmailVO;
import com.architek.oikos.shared.domain.valueobject.EntityId;

/**
 * Cross-feature adapter: delegates to party's public port-in use cases
 * (CreatePartyUseCase, GetPartyUseCase, LoadPartyIdByEmailUseCase), never to
 * party's repository directly (rule 6). Named distinctly from
 * user.infrastructure.adapter.PartyDirectoryAdapter to avoid a Spring bean name
 * collision between the two same-named classes.
 */
@Component
public class PropertyPartyDirectoryAdapter implements PartyDirectoryPort {

    private final CreatePartyUseCase createPartyUseCase;
    private final GetPartyUseCase getPartyUseCase;
    private final LoadPartyIdByEmailUseCase loadPartyIdByEmailUseCase;

    public PropertyPartyDirectoryAdapter(CreatePartyUseCase createPartyUseCase,
                                    GetPartyUseCase getPartyUseCase,
                                    LoadPartyIdByEmailUseCase loadPartyIdByEmailUseCase) {
        this.createPartyUseCase = createPartyUseCase;
        this.getPartyUseCase = getPartyUseCase;
        this.loadPartyIdByEmailUseCase = loadPartyIdByEmailUseCase;
    }

    @Override
    public EntityId createParty(PartyDetails details) {
        PartyId id = createPartyUseCase.create(new CreatePartyCommand(
                details.fullName(), details.partyType(), details.email(), null));
        return EntityId.of(id.asUuid());
    }

    @Override
    public Optional<EntityId> findIdByEmail(EmailVO email) {
        return loadPartyIdByEmailUseCase.loadByEmail(email).map(id -> EntityId.of(id.asUuid()));
    }

    @Override
    public PartyDetails getPartyById(EntityId partyId) {
        PartyView view = getPartyUseCase.getParty(new GetPartyQuery(PartyId.of(partyId.value())));
        return new PartyDetails(view.fullName(), view.partyType(), EmailVO.of(view.email()));
    }
}
