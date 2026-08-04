package com.architek.oikos.user.application.port.out;

import java.util.List;

import com.architek.oikos.shared.domain.valueobject.EntityId;

/**
 * Outbound port used to resolve the installments of a given unit, on behalf of the "my
 * installments" self-service view (a USER's consolidated view of dues across every lot they
 * own). Implemented in user.infrastructure.adapter by delegating to installment's public
 * port-in use cases - never to installment's repositories directly (rule 6).
 */
public interface InstallmentDirectoryPort {

    List<OwnedInstallmentView> listInstallmentsForUnit(EntityId unitId);
}
