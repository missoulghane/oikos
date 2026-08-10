package com.architek.oikos.installment.domain.repository;

import java.util.List;
import java.util.Optional;

import com.architek.oikos.installment.domain.model.Payment;
import com.architek.oikos.installment.domain.valueobject.PaymentId;
import com.architek.oikos.shared.domain.valueobject.EntityId;

public interface PaymentRepository {

    Payment save(Payment payment);

    Optional<Payment> findById(PaymentId id);

    List<Payment> findAllByUnitId(EntityId unitId);

    Optional<Payment> findLatestByPropertyId(EntityId propertyId);
}
