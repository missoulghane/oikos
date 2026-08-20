package com.architek.oikos.party.infrastructure.adapter;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import com.architek.oikos.party.domain.model.Party;
import com.architek.oikos.party.domain.repository.PartyRepository;
import com.architek.oikos.party.domain.valueobject.PartyId;
import com.architek.oikos.party.domain.valueobject.PartySearchCriteria;
import com.architek.oikos.party.domain.valueobject.PartySortField;
import com.architek.oikos.party.infrastructure.mapper.PartyPersistenceMapper;
import com.architek.oikos.party.infrastructure.persistence.PartyEntity;
import com.architek.oikos.party.infrastructure.persistence.PartyJpaRepository;
import com.architek.oikos.party.infrastructure.persistence.PartySpecifications;
import com.architek.oikos.shared.domain.pagination.Page;
import com.architek.oikos.shared.domain.pagination.PageRequest;
import com.architek.oikos.shared.domain.pagination.SortDirection;
import com.architek.oikos.shared.domain.valueobject.EmailVO;
import com.architek.oikos.shared.domain.valueobject.EntityId;

@Component
public class PartyRepositoryAdapter implements PartyRepository {

    private final PartyJpaRepository jpaRepository;
    private final PartyPersistenceMapper mapper;

    public PartyRepositoryAdapter(PartyJpaRepository jpaRepository, PartyPersistenceMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    public Party save(Party party) {
        PartyEntity entity = jpaRepository.findById(party.getId().asUuid()).orElseGet(PartyEntity::new);
        PartyEntity saved = jpaRepository.save(mapper.toEntity(party, entity));
        return mapper.toDomain(saved);
    }

    @Override
    public Optional<Party> findById(PartyId id) {
        return jpaRepository.findById(id.asUuid()).map(mapper::toDomain);
    }

    @Override
    public Optional<Party> findByPropertyIdAndEmail(EntityId propertyId, EmailVO email) {
        if (email == null) {
            return Optional.empty();
        }
        return jpaRepository.findByPropertyIdAndEmail(propertyId.value(), email.value()).map(mapper::toDomain);
    }

    @Override
    public Optional<Party> findByPropertyIdAndPhone(EntityId propertyId, String phone) {
        return jpaRepository.findByPropertyIdAndPhone(propertyId.value(), phone).map(mapper::toDomain);
    }

    @Override
    public boolean existsByPropertyIdAndEmail(EntityId propertyId, EmailVO email) {
        // Sans email, rien à comparer : plusieurs contacts d'une même copropriété
        // peuvent en être dépourvus, la contrainte d'unicité les laisse cohabiter.
        return email != null && jpaRepository.existsByPropertyIdAndEmail(propertyId.value(), email.value());
    }

    @Override
    public boolean existsByPropertyIdAndPhone(EntityId propertyId, String phone) {
        return jpaRepository.existsByPropertyIdAndPhone(propertyId.value(), phone);
    }

    @Override
    public Page<Party> findAll(PageRequest pageRequest, PartySearchCriteria criteria) {
        Pageable pageable = org.springframework.data.domain.PageRequest
                .of(pageRequest.pageNumber(), pageRequest.pageSize(), toSort(criteria));
        Specification<PartyEntity> specification = PartySpecifications.matching(criteria);
        org.springframework.data.domain.Page<PartyEntity> springPage = jpaRepository.findAll(specification, pageable);
        List<Party> content = springPage.getContent().stream().map(mapper::toDomain).toList();
        return Page.of(content, pageRequest.pageNumber(), pageRequest.pageSize(), springPage.getTotalElements());
    }

    /**
     * Sorted by the database, not in memory: the listing is paginated, so an
     * in-memory sort would only reorder the rows of the page just fetched.
     */
    private static Sort toSort(PartySearchCriteria criteria) {
        if (criteria.sortField() == null) {
            return Sort.unsorted();
        }
        String property = criteria.sortField() == PartySortField.EMAIL ? "email" : "fullName";
        Sort.Direction direction = criteria.sortDirection() == SortDirection.DESC
                ? Sort.Direction.DESC
                : Sort.Direction.ASC;
        // Ignoring case, so "alice" does not land after "Zoé" the way a raw
        // column sort would put every lowercase name last.
        return Sort.by(new Sort.Order(direction, property).ignoreCase());
    }

    @Override
    public void deleteById(PartyId id) {
        jpaRepository.deleteById(id.asUuid());
    }
}
