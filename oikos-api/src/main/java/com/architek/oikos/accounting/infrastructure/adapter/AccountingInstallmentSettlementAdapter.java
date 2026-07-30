package com.architek.oikos.accounting.infrastructure.adapter;

import java.math.BigDecimal;

import org.springframework.stereotype.Component;

import com.architek.oikos.accounting.application.port.out.InstallmentSettlementPort;
import com.architek.oikos.installment.application.port.in.UpdateInstallmentSettlementUseCase;
import com.architek.oikos.shared.domain.valueobject.EntityId;

/**
 * Cross-feature adapter: delegates to installment's public port-in
 * (UpdateInstallmentSettlementUseCase), never to installment's repository
 * or domain model directly (rule 4/6). Named distinctly from installment's
 * own adapters to avoid a Spring bean name collision between same-named
 * classes in different packages.
 */
@Component
public class AccountingInstallmentSettlementAdapter implements InstallmentSettlementPort {

    private final UpdateInstallmentSettlementUseCase updateInstallmentSettlementUseCase;

    public AccountingInstallmentSettlementAdapter(UpdateInstallmentSettlementUseCase updateInstallmentSettlementUseCase) {
        this.updateInstallmentSettlementUseCase = updateInstallmentSettlementUseCase;
    }

    @Override
    public void updateOutstandingAmount(EntityId installmentId, BigDecimal outstandingAmount) {
        updateInstallmentSettlementUseCase.update(installmentId, outstandingAmount);
    }
}
