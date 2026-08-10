package com.architek.oikos.installment.infrastructure.adapter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Component;

import com.architek.oikos.accounting.application.command.PostAdvanceRegularizationJournalEntryCommand;
import com.architek.oikos.accounting.application.port.in.GetUnitAdvanceBalanceUseCase;
import com.architek.oikos.accounting.application.port.in.ListUnitsWithAvailableAdvanceUseCase;
import com.architek.oikos.accounting.application.port.in.PostAdvanceRegularizationJournalEntryUseCase;
import com.architek.oikos.accounting.application.query.GetUnitAdvanceBalanceQuery;
import com.architek.oikos.accounting.application.query.ListUnitsWithAvailableAdvanceQuery;
import com.architek.oikos.installment.application.port.out.AdvanceRegularizationPort;
import com.architek.oikos.shared.domain.valueobject.EntityId;

@Component
public class InstallmentAdvanceRegularizationAdapter implements AdvanceRegularizationPort {

    private final GetUnitAdvanceBalanceUseCase getUnitAdvanceBalanceUseCase;
    private final ListUnitsWithAvailableAdvanceUseCase listUnitsWithAvailableAdvanceUseCase;
    private final PostAdvanceRegularizationJournalEntryUseCase postAdvanceRegularizationJournalEntryUseCase;

    public InstallmentAdvanceRegularizationAdapter(GetUnitAdvanceBalanceUseCase getUnitAdvanceBalanceUseCase,
                                                    ListUnitsWithAvailableAdvanceUseCase listUnitsWithAvailableAdvanceUseCase,
                                                    PostAdvanceRegularizationJournalEntryUseCase postAdvanceRegularizationJournalEntryUseCase) {
        this.getUnitAdvanceBalanceUseCase = getUnitAdvanceBalanceUseCase;
        this.listUnitsWithAvailableAdvanceUseCase = listUnitsWithAvailableAdvanceUseCase;
        this.postAdvanceRegularizationJournalEntryUseCase = postAdvanceRegularizationJournalEntryUseCase;
    }

    @Override
    public BigDecimal getAvailableAdvance(EntityId propertyId, EntityId unitId) {
        return getUnitAdvanceBalanceUseCase.get(new GetUnitAdvanceBalanceQuery(propertyId, unitId));
    }

    @Override
    public List<UnitAdvance> listUnitsWithAvailableAdvance(EntityId propertyId) {
        return listUnitsWithAvailableAdvanceUseCase.list(new ListUnitsWithAvailableAdvanceQuery(propertyId)).stream()
                .map(view -> new UnitAdvance(view.unitId(), view.amount()))
                .toList();
    }

    @Override
    public EntityId postRegularizationEntry(EntityId propertyId, EntityId unitId, LocalDate pieceDate,
                                             BigDecimal amount, EntityId createdByUserId) {
        return postAdvanceRegularizationJournalEntryUseCase.post(new PostAdvanceRegularizationJournalEntryCommand(
                propertyId, unitId, pieceDate, amount, createdByUserId)).value();
    }
}
