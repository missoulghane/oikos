package com.architek.oikos.party.domain.repository;

import java.util.Optional;

import com.architek.oikos.party.domain.model.Party;
import com.architek.oikos.party.domain.valueobject.PartyId;
import com.architek.oikos.party.domain.valueobject.PartySearchCriteria;
import com.architek.oikos.shared.domain.pagination.Page;
import com.architek.oikos.shared.domain.pagination.PageRequest;
import com.architek.oikos.shared.domain.valueobject.EmailVO;
import com.architek.oikos.shared.domain.valueobject.EntityId;

public interface PartyRepository {

    Party save(Party party);

    Optional<Party> findById(PartyId id);

    Optional<Party> findByPropertyIdAndEmail(EntityId propertyId, EmailVO email);

    Optional<Party> findByPropertyIdAndPhone(EntityId propertyId, String phone);

    boolean existsByPropertyIdAndEmail(EntityId propertyId, EmailVO email);

    boolean existsByPropertyIdAndPhone(EntityId propertyId, String phone);

    Page<Party> findAll(PageRequest pageRequest, PartySearchCriteria criteria);

    void deleteById(PartyId id);
}
