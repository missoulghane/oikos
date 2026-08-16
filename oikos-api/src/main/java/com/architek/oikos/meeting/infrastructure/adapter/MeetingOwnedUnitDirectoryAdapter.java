package com.architek.oikos.meeting.infrastructure.adapter;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

import com.architek.oikos.meeting.application.port.out.OwnedUnitDirectoryPort;
import com.architek.oikos.meeting.application.port.out.OwnedUnitInfo;
import com.architek.oikos.property.application.dto.PartyLotView;
import com.architek.oikos.property.application.port.in.ListLotsByPartyUseCase;
import com.architek.oikos.property.application.query.ListLotsByPartyQuery;
import com.architek.oikos.shared.domain.valueobject.EntityId;
import com.architek.oikos.user.application.dto.UserAccessView;
import com.architek.oikos.user.application.port.in.GetUserAccessUseCase;
import com.architek.oikos.user.application.query.GetUserAccessQuery;
import com.architek.oikos.user.domain.valueobject.UserId;

/**
 * Resolves "the lots this account owns" in two hops, both through public
 * port-in use cases (rule 6): the account's own parties from user, then those
 * parties' lots from property. The same pair PropertyAccessEvaluator.ownsUnit
 * relies on, read in the other direction.
 */
@Component
public class MeetingOwnedUnitDirectoryAdapter implements OwnedUnitDirectoryPort {

    private final GetUserAccessUseCase getUserAccessUseCase;
    private final ListLotsByPartyUseCase listLotsByPartyUseCase;

    public MeetingOwnedUnitDirectoryAdapter(GetUserAccessUseCase getUserAccessUseCase,
                                             ListLotsByPartyUseCase listLotsByPartyUseCase) {
        this.getUserAccessUseCase = getUserAccessUseCase;
        this.listLotsByPartyUseCase = listLotsByPartyUseCase;
    }

    @Override
    public List<OwnedUnitInfo> listOwnedUnits(EntityId userId) {
        UserAccessView access = getUserAccessUseCase.getAccess(new GetUserAccessQuery(UserId.of(userId.value())));
        List<OwnedUnitInfo> units = new ArrayList<>();
        for (String partyId : access.ownedPartyIds()) {
            for (PartyLotView lot : listLotsByPartyUseCase.listLots(new ListLotsByPartyQuery(EntityId.of(partyId)))) {
                units.add(new OwnedUnitInfo(EntityId.of(lot.unitId().value().value()), lot.unitNumber(),
                        lot.buildingName(), EntityId.of(lot.propertyId().value().value()), lot.propertyName()));
            }
        }
        return units;
    }
}
