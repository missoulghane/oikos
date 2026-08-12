package com.architek.oikos.installment.infrastructure.adapter;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import com.architek.oikos.installment.domain.model.Payment;
import com.architek.oikos.installment.domain.repository.PaymentRepository;
import com.architek.oikos.installment.domain.valueobject.PaymentId;
import com.architek.oikos.installment.infrastructure.mapper.PaymentPersistenceMapper;
import com.architek.oikos.installment.infrastructure.persistence.PaymentJpaRepository;
import com.architek.oikos.shared.domain.valueobject.EntityId;

@Component
public class PaymentRepositoryAdapter implements PaymentRepository {

    private final PaymentJpaRepository jpaRepository;
    private final PaymentPersistenceMapper mapper;

    public PaymentRepositoryAdapter(PaymentJpaRepository jpaRepository, PaymentPersistenceMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    public Payment save(Payment payment) {
        return mapper.toDomain(jpaRepository.save(mapper.toEntity(payment)));
    }

    @Override
    public Optional<Payment> findById(PaymentId id) {
        return jpaRepository.findById(id.asUuid()).map(mapper::toDomain);
    }

    @Override
    public List<Payment> findAllByUnitId(EntityId unitId) {
        return jpaRepository.findAllByUnitId(unitId.value()).stream().map(mapper::toDomain).toList();
    }

    @Override
    public List<Payment> findLatestByPropertyId(EntityId propertyId, int limit) {
        return jpaRepository
                .findByPropertyIdOrderByValueDateDescCreatedDateDesc(propertyId.value(), Pageable.ofSize(limit))
                .stream()
                .map(mapper::toDomain)
                .toList();
    }
}
