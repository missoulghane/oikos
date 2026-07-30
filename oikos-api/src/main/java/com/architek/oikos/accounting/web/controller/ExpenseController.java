package com.architek.oikos.accounting.web.controller;

import java.net.URI;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import com.architek.oikos.accounting.application.command.RecordExpenseCommand;
import com.architek.oikos.accounting.application.port.in.ListExpensesByPropertyUseCase;
import com.architek.oikos.accounting.application.port.in.RecordExpenseUseCase;
import com.architek.oikos.accounting.application.query.ListExpensesByPropertyQuery;
import com.architek.oikos.accounting.domain.valueobject.ExpenseId;
import com.architek.oikos.accounting.domain.valueobject.FinancialAccountId;
import com.architek.oikos.accounting.web.request.RecordExpenseRequest;
import com.architek.oikos.accounting.web.response.PagedExpenseResponse;
import com.architek.oikos.shared.domain.pagination.PageRequest;
import com.architek.oikos.shared.domain.valueobject.EntityId;

@RestController
public class ExpenseController {

    private final RecordExpenseUseCase recordExpenseUseCase;
    private final ListExpensesByPropertyUseCase listExpensesByPropertyUseCase;

    public ExpenseController(RecordExpenseUseCase recordExpenseUseCase,
                              ListExpensesByPropertyUseCase listExpensesByPropertyUseCase) {
        this.recordExpenseUseCase = recordExpenseUseCase;
        this.listExpensesByPropertyUseCase = listExpensesByPropertyUseCase;
    }

    @PreAuthorize("@propertyAccess.canWriteAccounting(authentication, #propertyId)")
    @PostMapping("/properties/{propertyId}/accounting/expenses")
    public ResponseEntity<Void> record(@PathVariable String propertyId,
                                       @Valid @RequestBody RecordExpenseRequest request,
                                       Authentication authentication) {
        ExpenseId id = recordExpenseUseCase.record(new RecordExpenseCommand(EntityId.of(propertyId),
                FinancialAccountId.of(request.financialAccountId()), request.date(), request.category(),
                request.provider(), request.amount(), request.description(), request.receiptReference(),
                currentUserId(authentication)));
        return ResponseEntity.created(URI.create("/api/v1/properties/" + propertyId + "/accounting/expenses/" + id))
                .build();
    }

    @PreAuthorize("@propertyAccess.canReadAccounting(authentication, #propertyId)")
    @GetMapping("/properties/{propertyId}/accounting/expenses")
    public PagedExpenseResponse list(@PathVariable String propertyId,
                                      @RequestParam(defaultValue = "0") int page,
                                      @RequestParam(defaultValue = "20") int size) {
        return PagedExpenseResponse.from(listExpensesByPropertyUseCase.list(
                new ListExpensesByPropertyQuery(EntityId.of(propertyId), PageRequest.of(page, size))));
    }

    private static EntityId currentUserId(Authentication authentication) {
        return EntityId.of(authentication.getName());
    }
}
