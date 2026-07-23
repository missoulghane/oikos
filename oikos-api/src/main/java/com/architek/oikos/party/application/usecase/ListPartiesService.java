package com.architek.oikos.party.application.usecase;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.architek.oikos.party.application.dto.PartyView;
import com.architek.oikos.party.application.port.in.ListPartiesUseCase;
import com.architek.oikos.party.application.query.ListPartiesQuery;
import com.architek.oikos.party.domain.repository.PartyRepository;
import com.architek.oikos.shared.domain.pagination.Page;

@Component
public class ListPartiesService implements ListPartiesUseCase {

    private final PartyRepository partyRepository;

    public ListPartiesService(PartyRepository partyRepository) {
        this.partyRepository = partyRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PartyView> listParties(ListPartiesQuery query) {
        return partyRepository.findAll(query.pageRequest(), query.criteria()).map(PartyView::from);
    }
}
