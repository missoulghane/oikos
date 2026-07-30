package com.architek.oikos.accounting.application.usecase;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.architek.oikos.accounting.application.dto.UnitAccountSummaryView;
import com.architek.oikos.accounting.application.port.in.ListUnitAccountSummariesByPropertyUseCase;
import com.architek.oikos.accounting.application.query.ListUnitAccountSummariesByPropertyQuery;
import com.architek.oikos.accounting.domain.model.UnitAccount;
import com.architek.oikos.accounting.domain.model.UnitAccountMovement;
import com.architek.oikos.accounting.domain.repository.UnitAccountMovementRepository;
import com.architek.oikos.accounting.domain.repository.UnitAccountRepository;
import com.architek.oikos.accounting.domain.valueobject.UnitAccountMovementType;

/** Spec &sect;16 "Units": total du / total paye / avance disponible / solde actuel, per unit. */
@Component
public class ListUnitAccountSummariesByPropertyService implements ListUnitAccountSummariesByPropertyUseCase {

    private final UnitAccountRepository unitAccountRepository;
    private final UnitAccountMovementRepository unitAccountMovementRepository;

    public ListUnitAccountSummariesByPropertyService(UnitAccountRepository unitAccountRepository,
                                                       UnitAccountMovementRepository unitAccountMovementRepository) {
        this.unitAccountRepository = unitAccountRepository;
        this.unitAccountMovementRepository = unitAccountMovementRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<UnitAccountSummaryView> list(ListUnitAccountSummariesByPropertyQuery query) {
        return unitAccountRepository.findAllByPropertyId(query.propertyId()).stream()
                .map(this::toSummary)
                .toList();
    }

    private UnitAccountSummaryView toSummary(UnitAccount unitAccount) {
        List<UnitAccountMovement> movements = unitAccountMovementRepository.findAllByUnitAccountId(unitAccount.getId());

        BigDecimal totalDue = movements.stream()
                .filter(movement -> movement.getType() == UnitAccountMovementType.FUND_CALL)
                .map(movement -> movement.getAmount().value())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalPaid = movements.stream()
                .filter(movement -> movement.getType() == UnitAccountMovementType.PAYMENT)
                .map(movement -> movement.getAmount().value())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal balance = unitAccount.getBalance();
        BigDecimal availableAdvance = balance.signum() > 0 ? balance : BigDecimal.ZERO;

        return new UnitAccountSummaryView(unitAccount.getUnitId(), totalDue, totalPaid, availableAdvance, balance);
    }
}
