package com.architek.oikos.accounting.web.controller;

import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import com.architek.oikos.accounting.application.port.in.ListExpensesByPropertyUseCase;
import com.architek.oikos.accounting.application.query.ListExpensesByPropertyQuery;
import com.architek.oikos.accounting.web.response.ExpenseResponse;
import com.architek.oikos.shared.domain.valueobject.EntityId;

/**
 * Read-only listing (writes go through SupplierPaymentController, Partie 2:
 * an expense is now always created together with its payment - see
 * RecordSupplierPaymentService).
 */
@RestController
public class ExpenseController {

    private final ListExpensesByPropertyUseCase listExpensesByPropertyUseCase;

    public ExpenseController(ListExpensesByPropertyUseCase listExpensesByPropertyUseCase) {
        this.listExpensesByPropertyUseCase = listExpensesByPropertyUseCase;
    }

    @PreAuthorize("@propertyAccess.canReadAccounting(authentication, #propertyId)")
    @GetMapping("/properties/{propertyId}/accounting/expenses")
    public List<ExpenseResponse> listByProperty(@PathVariable String propertyId) {
        return listExpensesByPropertyUseCase.list(new ListExpensesByPropertyQuery(EntityId.of(propertyId))).stream()
                .map(ExpenseResponse::from)
                .toList();
    }
}
