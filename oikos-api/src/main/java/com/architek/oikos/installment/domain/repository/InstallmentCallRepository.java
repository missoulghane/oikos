package com.architek.oikos.installment.domain.repository;

import java.time.YearMonth;
import java.util.Optional;

import com.architek.oikos.installment.domain.model.InstallmentCall;
import com.architek.oikos.installment.domain.valueobject.InstallmentCallId;
import com.architek.oikos.shared.domain.pagination.Page;
import com.architek.oikos.shared.domain.pagination.PageRequest;
import com.architek.oikos.shared.domain.valueobject.EntityId;

public interface InstallmentCallRepository {

    InstallmentCall save(InstallmentCall installmentCall);

    Optional<InstallmentCall> findById(InstallmentCallId id);

    boolean existsByPropertyIdAndPeriod(EntityId propertyId, YearMonth period);

    Page<InstallmentCall> findPageByPropertyId(EntityId propertyId, PageRequest pageRequest);
}
