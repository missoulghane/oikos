package com.architek.oikos.accounting.application.usecase;

import java.util.List;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.architek.oikos.accounting.application.dto.FinancialAccountView;
import com.architek.oikos.accounting.application.port.in.ListFinancialAccountsByPropertyUseCase;
import com.architek.oikos.accounting.application.query.ListFinancialAccountsByPropertyQuery;
import com.architek.oikos.accounting.domain.repository.FinancialAccountRepository;

@Component
public class ListFinancialAccountsByPropertyService implements ListFinancialAccountsByPropertyUseCase {

    private final FinancialAccountRepository financialAccountRepository;

    public ListFinancialAccountsByPropertyService(FinancialAccountRepository financialAccountRepository) {
        this.financialAccountRepository = financialAccountRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<FinancialAccountView> list(ListFinancialAccountsByPropertyQuery query) {
        return financialAccountRepository.findAllByPropertyId(query.propertyId()).stream()
                .map(FinancialAccountView::from)
                .toList();
    }
}
