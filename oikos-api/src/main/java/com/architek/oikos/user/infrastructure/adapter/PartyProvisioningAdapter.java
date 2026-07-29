package com.architek.oikos.user.infrastructure.adapter;

import org.springframework.stereotype.Component;

import com.architek.oikos.party.application.command.CreatePartyCommand;
import com.architek.oikos.party.application.port.in.CreatePartyUseCase;
import com.architek.oikos.party.domain.valueobject.PartyId;
import com.architek.oikos.shared.domain.valueobject.EntityId;
import com.architek.oikos.shared.domain.valueobject.PartyType;
import com.architek.oikos.user.application.port.out.PartyProvisioningDetails;
import com.architek.oikos.user.application.port.out.PartyProvisioningPort;

/**
 * Cross-feature adapter: delegates to party's public CreatePartyUseCase,
 * never to party's repository directly (rule 6). A newly registered property
 * manager's Party always backs onto an INDIVIDUAL party - a company cannot
 * log in on its own behalf.
 */
@Component
public class PartyProvisioningAdapter implements PartyProvisioningPort {

    private final CreatePartyUseCase createPartyUseCase;

    public PartyProvisioningAdapter(CreatePartyUseCase createPartyUseCase) {
        this.createPartyUseCase = createPartyUseCase;
    }

    @Override
    public EntityId createParty(PartyProvisioningDetails details) {
        PartyId id = createPartyUseCase.create(new CreatePartyCommand(
                details.propertyId(), details.fullName(), PartyType.INDIVIDUAL, details.email(), details.phone()));
        return EntityId.of(id.asUuid());
    }
}
