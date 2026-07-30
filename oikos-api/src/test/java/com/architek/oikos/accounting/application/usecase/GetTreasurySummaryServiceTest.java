package com.architek.oikos.accounting.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.architek.oikos.accounting.application.dto.TreasurySummaryView;
import com.architek.oikos.accounting.application.query.GetTreasurySummaryQuery;
import com.architek.oikos.accounting.domain.model.FinancialAccount;
import com.architek.oikos.accounting.domain.repository.FinancialAccountRepository;
import com.architek.oikos.accounting.domain.valueobject.FinancialAccountId;
import com.architek.oikos.accounting.domain.valueobject.FinancialAccountType;
import com.architek.oikos.accounting.domain.valueobject.FinancialEntryDirection;
import com.architek.oikos.shared.domain.valueobject.EntityId;

@ExtendWith(MockitoExtension.class)
class GetTreasurySummaryServiceTest {

    @Mock
    private FinancialAccountRepository financialAccountRepository;

    private GetTreasurySummaryService newService() {
        return new GetTreasurySummaryService(financialAccountRepository);
    }

    @Test
    void sums_cash_and_bank_and_mobile_money_accounts_separately_and_together() {
        EntityId propertyId = EntityId.newId();
        FinancialAccount cash = FinancialAccount.create(FinancialAccountId.newId(), propertyId, "Caisse",
                FinancialAccountType.CASH, "MAD").applyEntry(FinancialEntryDirection.IN, new BigDecimal("1000"));
        FinancialAccount bank = FinancialAccount.create(FinancialAccountId.newId(), propertyId, "Banque",
                FinancialAccountType.BANK, "MAD").applyEntry(FinancialEntryDirection.IN, new BigDecimal("500"));
        FinancialAccount mobileMoney = FinancialAccount.create(FinancialAccountId.newId(), propertyId, "Mobile Money",
                FinancialAccountType.MOBILE_MONEY, "MAD").applyEntry(FinancialEntryDirection.IN, new BigDecimal("200"));

        when(financialAccountRepository.findAllByPropertyId(propertyId)).thenReturn(List.of(cash, bank, mobileMoney));

        TreasurySummaryView summary = newService().get(new GetTreasurySummaryQuery(propertyId));

        assertThat(summary.cashBalance()).isEqualByComparingTo("1000");
        assertThat(summary.bankBalance()).isEqualByComparingTo("700");
        assertThat(summary.totalBalance()).isEqualByComparingTo("1700");
    }
}
