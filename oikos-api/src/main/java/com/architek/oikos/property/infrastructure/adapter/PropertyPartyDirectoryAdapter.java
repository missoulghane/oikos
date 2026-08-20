package com.architek.oikos.property.infrastructure.adapter;

import java.util.Optional;

import org.springframework.stereotype.Component;

import com.architek.oikos.party.application.command.CreatePartyCommand;
import com.architek.oikos.party.application.dto.PartyView;
import com.architek.oikos.party.application.port.in.CreatePartyUseCase;
import com.architek.oikos.party.application.port.in.GetPartyUseCase;
import com.architek.oikos.party.application.port.in.LoadPartyIdByEmailUseCase;
import com.architek.oikos.party.application.port.in.LoadPartyIdByPhoneUseCase;
import com.architek.oikos.party.application.query.GetPartyQuery;
import com.architek.oikos.party.domain.valueobject.PartyId;
import com.architek.oikos.property.application.port.out.PartyDetails;
import com.architek.oikos.property.application.port.out.PartyDirectoryPort;
import com.architek.oikos.shared.domain.valueobject.EmailVO;
import com.architek.oikos.shared.domain.valueobject.EntityId;

/**
 * Cross-feature adapter: delegates to party's public port-in use cases
 * (CreatePartyUseCase, GetPartyUseCase, LoadPartyIdByEmail/PhoneUseCase), never to
 * party's repository directly (rule 6). Named distinctly from
 * user.infrastructure.adapter.PartyDirectoryAdapter to avoid a Spring bean name
 * collision between the two same-named classes.
 */
@Component
public class PropertyPartyDirectoryAdapter implements PartyDirectoryPort {

    private final CreatePartyUseCase createPartyUseCase;
    private final GetPartyUseCase getPartyUseCase;
    private final LoadPartyIdByEmailUseCase loadPartyIdByEmailUseCase;
    private final LoadPartyIdByPhoneUseCase loadPartyIdByPhoneUseCase;

    public PropertyPartyDirectoryAdapter(CreatePartyUseCase createPartyUseCase,
                                    GetPartyUseCase getPartyUseCase,
                                    LoadPartyIdByEmailUseCase loadPartyIdByEmailUseCase,
                                    LoadPartyIdByPhoneUseCase loadPartyIdByPhoneUseCase) {
        this.createPartyUseCase = createPartyUseCase;
        this.getPartyUseCase = getPartyUseCase;
        this.loadPartyIdByEmailUseCase = loadPartyIdByEmailUseCase;
        this.loadPartyIdByPhoneUseCase = loadPartyIdByPhoneUseCase;
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
    public Optional<EntityId> findIdByPhone(String phone, EntityId propertyId) {
        return loadPartyIdByPhoneUseCase.loadByPhone(propertyId, phone).map(id -> EntityId.of(id.asUuid()));
    }

    @Override
    public PartyDetails getPartyById(EntityId partyId) {
        PartyView view = getPartyUseCase.getParty(new GetPartyQuery(PartyId.of(partyId.value())));
        // view.email() est nul pour un contact qui n'a qu'un téléphone.
        return new PartyDetails(view.fullName(), view.partyType(),
                view.email() != null ? EmailVO.of(view.email()) : null, view.phone());
    }
}
