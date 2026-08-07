package com.architek.oikos.installment.domain.repository;

import java.util.List;
import java.util.Optional;

import com.architek.oikos.installment.domain.model.Installment;
import com.architek.oikos.installment.domain.valueobject.InstallmentCallId;
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

    void deleteAllByInstallmentCallId(InstallmentCallId installmentCallId);
}
