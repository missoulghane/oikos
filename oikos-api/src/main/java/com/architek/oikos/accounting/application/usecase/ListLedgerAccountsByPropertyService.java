package com.architek.oikos.accounting.application.usecase;

import java.util.List;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.architek.oikos.accounting.application.dto.LedgerAccountView;
import com.architek.oikos.accounting.application.port.in.ListLedgerAccountsByPropertyUseCase;
import com.architek.oikos.accounting.application.query.ListLedgerAccountsByPropertyQuery;
import com.architek.oikos.accounting.domain.repository.LedgerAccountRepository;

@Component
public class ListLedgerAccountsByPropertyService implements ListLedgerAccountsByPropertyUseCase {

    private final LedgerAccountRepository ledgerAccountRepository;

    public ListLedgerAccountsByPropertyService(LedgerAccountRepository ledgerAccountRepository) {
        this.ledgerAccountRepository = ledgerAccountRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<LedgerAccountView> list(ListLedgerAccountsByPropertyQuery query) {
        return ledgerAccountRepository.findAllVisibleToProperty(query.propertyId()).stream()
                .map(LedgerAccountView::from)
                .toList();
    }
}
