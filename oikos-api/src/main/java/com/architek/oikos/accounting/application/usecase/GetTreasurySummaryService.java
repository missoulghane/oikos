package com.architek.oikos.accounting.application.usecase;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.architek.oikos.accounting.application.dto.TreasurySummaryView;
import com.architek.oikos.accounting.application.port.in.GetTreasurySummaryUseCase;
import com.architek.oikos.accounting.application.query.GetTreasurySummaryQuery;
import com.architek.oikos.accounting.domain.model.FinancialAccount;
import com.architek.oikos.accounting.domain.repository.FinancialAccountRepository;
import com.architek.oikos.accounting.domain.valueobject.FinancialAccountType;

/** Spec &sect;16 "Tresorerie": solde caisse / solde banques (banque + mobile money) / solde total. */
@Component
public class GetTreasurySummaryService implements GetTreasurySummaryUseCase {

    private final FinancialAccountRepository financialAccountRepository;

    public GetTreasurySummaryService(FinancialAccountRepository financialAccountRepository) {
        this.financialAccountRepository = financialAccountRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public TreasurySummaryView get(GetTreasurySummaryQuery query) {
        List<FinancialAccount> accounts = financialAccountRepository.findAllByPropertyId(query.propertyId());

        BigDecimal cashBalance = sumByType(accounts, FinancialAccountType.CASH);
        BigDecimal bankBalance = sumByTypes(accounts, FinancialAccountType.BANK, FinancialAccountType.MOBILE_MONEY);
        BigDecimal totalBalance = cashBalance.add(bankBalance);

        return new TreasurySummaryView(cashBalance, bankBalance, totalBalance);
    }

    private BigDecimal sumByType(List<FinancialAccount> accounts, FinancialAccountType type) {
        return accounts.stream().filter(account -> account.getType() == type).map(FinancialAccount::getBalance)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal sumByTypes(List<FinancialAccount> accounts, FinancialAccountType... types) {
        return accounts.stream().filter(account -> containsType(types, account.getType()))
                .map(FinancialAccount::getBalance).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private boolean containsType(FinancialAccountType[] types, FinancialAccountType candidate) {
        for (FinancialAccountType type : types) {
            if (type == candidate) {
                return true;
            }
        }
        return false;
    }
}
