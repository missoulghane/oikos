package com.architek.oikos.installment.infrastructure.adapter;

import java.util.List;

import org.springframework.stereotype.Component;

import com.architek.oikos.accounting.application.dto.MovementView;
import com.architek.oikos.accounting.application.port.in.ListMovementsForAllocationUseCase;
import com.architek.oikos.accounting.application.query.ListMovementsForAllocationQuery;
import com.architek.oikos.accounting.domain.valueobject.AccountId;
import com.architek.oikos.accounting.domain.valueobject.MovementDirection;
import com.architek.oikos.installment.application.port.out.AccountMovement;
import com.architek.oikos.installment.application.port.out.AccountMovementsPort;
import com.architek.oikos.shared.domain.valueobject.EntityId;

/**
 * Cross-feature adapter: reads accounting's ledger by delegating to its public
 * port-in (ListMovementsForAllocationUseCase), never to accounting's
 * repositories directly (rule 4).
 */
@Component
public class InstallmentAccountMovementsAdapter implements AccountMovementsPort {

    private final ListMovementsForAllocationUseCase listMovementsForAllocationUseCase;

    public InstallmentAccountMovementsAdapter(ListMovementsForAllocationUseCase listMovementsForAllocationUseCase) {
        this.listMovementsForAllocationUseCase = listMovementsForAllocationUseCase;
    }

    @Override
    public List<AccountMovement> listCreditMovements(EntityId accountId) {
        return listMovementsForAllocationUseCase
                .listMovements(new ListMovementsForAllocationQuery(AccountId.of(accountId.value())))
                .stream()
                .filter(view -> view.direction() == MovementDirection.CREDIT)
                .map(view -> new AccountMovement(view.id().value(), view.occurredOn(), view.amount()))
                .toList();
    }
}
