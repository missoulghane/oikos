package com.architek.oikos.party.application.port.in;

import com.architek.oikos.party.application.dto.PartyView;
import com.architek.oikos.party.application.query.ListPartiesQuery;
import com.architek.oikos.shared.domain.pagination.Page;

public interface ListPartiesUseCase {

    Page<PartyView> listParties(ListPartiesQuery query);
}
