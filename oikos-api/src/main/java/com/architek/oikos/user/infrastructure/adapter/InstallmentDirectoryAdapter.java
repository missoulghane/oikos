package com.architek.oikos.user.infrastructure.adapter;

import java.util.List;

import org.springframework.stereotype.Component;

import com.architek.oikos.installment.application.dto.InstallmentView;
import com.architek.oikos.installment.application.port.in.ListInstallmentsByUnitUseCase;
import com.architek.oikos.installment.application.query.ListInstallmentsByUnitQuery;
import com.architek.oikos.shared.domain.valueobject.EntityId;
import com.architek.oikos.user.application.port.out.InstallmentDirectoryPort;
import com.architek.oikos.user.application.port.out.OwnedInstallmentStatus;
import com.architek.oikos.user.application.port.out.OwnedInstallmentView;

@Component
public class InstallmentDirectoryAdapter implements InstallmentDirectoryPort {

    private final ListInstallmentsByUnitUseCase listInstallmentsByUnitUseCase;

    public InstallmentDirectoryAdapter(ListInstallmentsByUnitUseCase listInstallmentsByUnitUseCase) {
        this.listInstallmentsByUnitUseCase = listInstallmentsByUnitUseCase;
    }

    @Override
    public List<OwnedInstallmentView> listInstallmentsForUnit(EntityId unitId) {
        return listInstallmentsByUnitUseCase.listInstallments(new ListInstallmentsByUnitQuery(unitId)).stream()
                .map(InstallmentDirectoryAdapter::toOwnedInstallmentView)
                .toList();
    }

    private static OwnedInstallmentView toOwnedInstallmentView(InstallmentView view) {
        // .name() only, never the enum type itself - installment.domain.valueobject.InstallmentStatus
        // must not be named in this module (rule 4), same as OwnedInstallmentStatus's own rationale.
        return new OwnedInstallmentView(EntityId.of(view.id().toString()), view.unitId(), view.dueDate(),
                view.amount(), view.outstandingAmount(), OwnedInstallmentStatus.valueOf(view.status().name()));
    }
}
