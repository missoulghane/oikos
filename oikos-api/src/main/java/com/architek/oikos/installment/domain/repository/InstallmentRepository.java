package com.architek.oikos.installment.domain.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import com.architek.oikos.installment.domain.model.Installment;
import com.architek.oikos.installment.domain.valueobject.InstallmentCallId;
import com.architek.oikos.installment.domain.valueobject.InstallmentCollectionSummary;
import com.architek.oikos.installment.domain.valueobject.InstallmentFilter;
import com.architek.oikos.installment.domain.valueobject.InstallmentId;
import com.architek.oikos.shared.domain.pagination.Page;
import com.architek.oikos.shared.domain.pagination.PageRequest;
import com.architek.oikos.shared.domain.valueobject.EntityId;

public interface InstallmentRepository {

    Installment save(Installment installment);

    Optional<Installment> findById(InstallmentId id);

    List<Installment> findAllByUnitId(EntityId unitId);

    List<Installment> findAllByInstallmentCallId(InstallmentCallId installmentCallId);

    Page<Installment> findPageByUnitIds(List<EntityId> unitIds, InstallmentFilter filter, PageRequest pageRequest);

    /**
     * How much of these units' installments is unpaid and already due, as of
     * the given date. An aggregate rather than a page: the caller wants two
     * numbers, and reading every row to add them up would page through the
     * whole copropriété to display one badge.
     */
    InstallmentCollectionSummary summariseCollectible(List<EntityId> unitIds, LocalDate asOf);

    void deleteAllByInstallmentCallId(InstallmentCallId installmentCallId);
}
