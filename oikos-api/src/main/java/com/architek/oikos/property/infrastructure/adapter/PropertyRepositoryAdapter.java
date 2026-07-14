package com.architek.oikos.property.infrastructure.adapter;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import com.architek.oikos.property.domain.model.Property;
import com.architek.oikos.property.domain.repository.PropertyRepository;
import com.architek.oikos.property.domain.valueobject.PropertyId;
import com.architek.oikos.property.infrastructure.mapper.PropertyPersistenceMapper;
import com.architek.oikos.property.infrastructure.persistence.PropertyEntity;
import com.architek.oikos.property.infrastructure.persistence.PropertyJpaRepository;
import com.architek.oikos.shared.domain.pagination.Page;
import com.architek.oikos.shared.domain.pagination.PageRequest;

@Component
public class PropertyRepositoryAdapter implements PropertyRepository {

    private final PropertyJpaRepository jpaRepository;
    private final PropertyPersistenceMapper mapper;

    public PropertyRepositoryAdapter(PropertyJpaRepository jpaRepository, PropertyPersistenceMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    public Property save(Property property) {
        PropertyEntity entity = jpaRepository.findById(property.getId().asUuid()).orElseGet(PropertyEntity::new);
        PropertyEntity saved = jpaRepository.save(mapper.toEntity(property, entity));
        return mapper.toDomain(saved);
    }

    @Override
    public Optional<Property> findById(PropertyId id) {
        return jpaRepository.findById(id.asUuid()).map(mapper::toDomain);
    }

    @Override
    public Page<Property> findAll(PageRequest pageRequest) {
        Pageable pageable = Pageable.ofSize(pageRequest.pageSize()).withPage(pageRequest.pageNumber());
        org.springframework.data.domain.Page<PropertyEntity> springPage = jpaRepository.findAll(pageable);
        List<Property> content = springPage.getContent().stream().map(mapper::toDomain).toList();
        return Page.of(content, pageRequest.pageNumber(), pageRequest.pageSize(), springPage.getTotalElements());
    }
}
