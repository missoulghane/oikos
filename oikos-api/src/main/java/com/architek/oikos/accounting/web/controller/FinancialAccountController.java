package com.architek.oikos.accounting.web.controller;

import java.net.URI;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import com.architek.oikos.accounting.application.command.CreateFinancialAccountCommand;
import com.architek.oikos.accounting.application.command.RecordExceptionalDepositCommand;
import com.architek.oikos.accounting.application.command.TransferBetweenFinancialAccountsCommand;
import com.architek.oikos.accounting.application.port.in.CreateFinancialAccountUseCase;
import com.architek.oikos.accounting.application.port.in.ListFinancialAccountsByPropertyUseCase;
import com.architek.oikos.accounting.application.port.in.RecordExceptionalDepositUseCase;
import com.architek.oikos.accounting.application.port.in.TransferBetweenFinancialAccountsUseCase;
import com.architek.oikos.accounting.application.query.ListFinancialAccountsByPropertyQuery;
import com.architek.oikos.accounting.domain.valueobject.FinancialAccountId;
import com.architek.oikos.accounting.web.request.CreateFinancialAccountRequest;
import com.architek.oikos.accounting.web.request.RecordExceptionalDepositRequest;
import com.architek.oikos.accounting.web.request.TransferBetweenFinancialAccountsRequest;
import com.architek.oikos.accounting.web.response.FinancialAccountResponse;
import com.architek.oikos.shared.domain.valueobject.EntityId;

@RestController
public class FinancialAccountController {

    private final CreateFinancialAccountUseCase createFinancialAccountUseCase;
    private final ListFinancialAccountsByPropertyUseCase listFinancialAccountsByPropertyUseCase;
    private final TransferBetweenFinancialAccountsUseCase transferBetweenFinancialAccountsUseCase;
    private final RecordExceptionalDepositUseCase recordExceptionalDepositUseCase;

    public FinancialAccountController(CreateFinancialAccountUseCase createFinancialAccountUseCase,
                                       ListFinancialAccountsByPropertyUseCase listFinancialAccountsByPropertyUseCase,
                                       TransferBetweenFinancialAccountsUseCase transferBetweenFinancialAccountsUseCase,
                                       RecordExceptionalDepositUseCase recordExceptionalDepositUseCase) {
        this.createFinancialAccountUseCase = createFinancialAccountUseCase;
        this.listFinancialAccountsByPropertyUseCase = listFinancialAccountsByPropertyUseCase;
        this.transferBetweenFinancialAccountsUseCase = transferBetweenFinancialAccountsUseCase;
        this.recordExceptionalDepositUseCase = recordExceptionalDepositUseCase;
    }

    @PreAuthorize("@propertyAccess.canWriteAccounting(authentication, #propertyId)")
    @PostMapping("/properties/{propertyId}/accounting/financial-accounts")
    public ResponseEntity<Void> create(@PathVariable String propertyId,
                                        @Valid @RequestBody CreateFinancialAccountRequest request) {
        FinancialAccountId id = createFinancialAccountUseCase.create(new CreateFinancialAccountCommand(
                EntityId.of(propertyId), request.name(), request.type(), request.currency()));
        return ResponseEntity
                .created(URI.create("/api/v1/properties/" + propertyId + "/accounting/financial-accounts/" + id))
                .build();
    }

    @PreAuthorize("@propertyAccess.canReadAccounting(authentication, #propertyId)")
    @GetMapping("/properties/{propertyId}/accounting/financial-accounts")
    public List<FinancialAccountResponse> list(@PathVariable String propertyId) {
        return listFinancialAccountsByPropertyUseCase
                .list(new ListFinancialAccountsByPropertyQuery(EntityId.of(propertyId))).stream()
                .map(FinancialAccountResponse::from)
                .toList();
    }

    @PreAuthorize("@propertyAccess.canWriteAccounting(authentication, #propertyId)")
    @PostMapping("/properties/{propertyId}/accounting/financial-accounts/transfers")
    public ResponseEntity<Void> transfer(@PathVariable String propertyId,
                                          @Valid @RequestBody TransferBetweenFinancialAccountsRequest request,
                                          Authentication authentication) {
        transferBetweenFinancialAccountsUseCase.transfer(new TransferBetweenFinancialAccountsCommand(
                EntityId.of(propertyId), FinancialAccountId.of(request.fromAccountId()),
                FinancialAccountId.of(request.toAccountId()), request.amount(), request.date(), request.label(),
                currentUserId(authentication)));
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("@propertyAccess.canWriteAccounting(authentication, #propertyId)")
    @PostMapping("/properties/{propertyId}/accounting/financial-accounts/deposits")
    public ResponseEntity<Void> recordDeposit(@PathVariable String propertyId,
                                               @Valid @RequestBody RecordExceptionalDepositRequest request,
                                               Authentication authentication) {
        recordExceptionalDepositUseCase.record(new RecordExceptionalDepositCommand(EntityId.of(propertyId),
                FinancialAccountId.of(request.financialAccountId()), request.amount(), request.date(),
                request.label(), currentUserId(authentication)));
        return ResponseEntity.noContent().build();
    }

    private static EntityId currentUserId(Authentication authentication) {
        return EntityId.of(authentication.getName());
    }
}
