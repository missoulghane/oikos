package com.architek.oikos.property.application.port.in;

import java.util.List;

import com.architek.oikos.property.application.dto.PartyLotView;
import com.architek.oikos.property.application.query.ListLotsByPartyQuery;

public interface ListLotsByPartyUseCase {

    List<PartyLotView> listLots(ListLotsByPartyQuery query);
}
