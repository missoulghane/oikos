package com.architek.oikos.party.domain.repository;

import java.util.Optional;

import com.architek.oikos.party.domain.model.Party;
import com.architek.oikos.party.domain.valueobject.PartyId;
import com.architek.oikos.party.domain.valueobject.PartySearchCriteria;
import com.architek.oikos.shared.domain.pagination.Page;
import com.architek.oikos.shared.domain.pagination.PageRequest;
import com.architek.oikos.shared.domain.valueobject.EmailVO;

public interface PartyRepository {

    Party save(Party party);

    Optional<Party> findById(PartyId id);

    Optional<Party> findByEmail(EmailVO email);

    Optional<Party> findByPhone(String phone);

    boolean existsByEmail(EmailVO email);

    Page<Party> findAll(PageRequest pageRequest, PartySearchCriteria criteria);

    void deleteById(PartyId id);
}
