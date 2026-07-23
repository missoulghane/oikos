package com.architek.oikos.user.infrastructure.adapter;

import java.util.Optional;

import org.springframework.stereotype.Component;

import com.architek.oikos.party.application.command.CreatePartyCommand;
import com.architek.oikos.party.application.command.UpdatePartyCommand;
import com.architek.oikos.party.application.dto.PartyView;
import com.architek.oikos.party.application.port.in.CreatePartyUseCase;
import com.architek.oikos.party.application.port.in.GetPartyUseCase;
import com.architek.oikos.party.application.port.in.LoadPartyIdByEmailUseCase;
import com.architek.oikos.party.application.port.in.LoadPartyIdByPhoneUseCase;
import com.architek.oikos.party.application.port.in.UpdatePartyUseCase;
import com.architek.oikos.party.application.query.GetPartyQuery;
import com.architek.oikos.party.domain.valueobject.PartyId;
import com.architek.oikos.shared.domain.valueobject.EmailVO;
import com.architek.oikos.shared.domain.valueobject.EntityId;
import com.architek.oikos.shared.domain.valueobject.PartyType;
import com.architek.oikos.user.application.port.out.PartyDetails;
import com.architek.oikos.user.application.port.out.PartyDirectoryPort;

/**
 * Cross-feature adapter: delegates to party's public port-in use cases
 * (CreatePartyUseCase, GetPartyUseCase, UpdatePartyUseCase,
 * LoadPartyIdByEmailUseCase, LoadPartyIdByPhoneUseCase), never to party's
 * repository directly (rule 6). Application users always back onto an
 * INDIVIDUAL party - a company cannot log in on its own behalf.
 */
@Component
public class UserPartyDirectoryAdapter implements PartyDirectoryPort {

    private final CreatePartyUseCase createPartyUseCase;
    private final GetPartyUseCase getPartyUseCase;
    private final UpdatePartyUseCase updatePartyUseCase;
    private final LoadPartyIdByEmailUseCase loadPartyIdByEmailUseCase;
    private final LoadPartyIdByPhoneUseCase loadPartyIdByPhoneUseCase;

    public UserPartyDirectoryAdapter(CreatePartyUseCase createPartyUseCase,
                                    GetPartyUseCase getPartyUseCase,
                                    UpdatePartyUseCase updatePartyUseCase,
                                    LoadPartyIdByEmailUseCase loadPartyIdByEmailUseCase,
                                    LoadPartyIdByPhoneUseCase loadPartyIdByPhoneUseCase) {
        this.createPartyUseCase = createPartyUseCase;
        this.getPartyUseCase = getPartyUseCase;
        this.updatePartyUseCase = updatePartyUseCase;
        this.loadPartyIdByEmailUseCase = loadPartyIdByEmailUseCase;
        this.loadPartyIdByPhoneUseCase = loadPartyIdByPhoneUseCase;
    }

    @Override
    public EntityId createParty(PartyDetails details) {
        PartyId id = createPartyUseCase.create(new CreatePartyCommand(
                details.fullName(), PartyType.INDIVIDUAL, details.email(), details.phone()));
        return EntityId.of(id.asUuid());
    }

    @Override
    public PartyDetails getPartyById(EntityId partyId) {
        PartyView view = getPartyUseCase.getParty(new GetPartyQuery(PartyId.of(partyId.value())));
        return toDetails(view);
    }

    @Override
    public PartyDetails updateParty(EntityId partyId, PartyDetails details) {
        PartyView view = updatePartyUseCase.update(new UpdatePartyCommand(
                PartyId.of(partyId.value()), details.fullName(), PartyType.INDIVIDUAL, details.email(), details.phone()));
        return toDetails(view);
    }

    @Override
    public Optional<EntityId> findIdByEmail(EmailVO email) {
        return loadPartyIdByEmailUseCase.loadByEmail(email).map(id -> EntityId.of(id.asUuid()));
    }

    @Override
    public Optional<EntityId> findIdByPhone(String phone) {
        return loadPartyIdByPhoneUseCase.loadByPhone(phone).map(id -> EntityId.of(id.asUuid()));
    }

    private static PartyDetails toDetails(PartyView view) {
        return new PartyDetails(view.fullName(), EmailVO.of(view.email()), view.phone());
    }
}
