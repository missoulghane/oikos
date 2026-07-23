package com.architek.oikos.accounting.web.controller;

import java.net.URI;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import com.architek.oikos.accounting.application.command.CreateAccountCommand;
import com.architek.oikos.accounting.application.command.RecordPaymentCommand;
import com.architek.oikos.accounting.application.port.in.CreateAccountUseCase;
import com.architek.oikos.accounting.application.port.in.GetAccountBalanceUseCase;
import com.architek.oikos.accounting.application.port.in.GetAccountByHolderUseCase;
import com.architek.oikos.accounting.application.port.in.GetAccountUseCase;
import com.architek.oikos.accounting.application.port.in.ListMovementsUseCase;
import com.architek.oikos.accounting.application.port.in.RecordPaymentUseCase;
import com.architek.oikos.accounting.application.query.GetAccountBalanceQuery;
import com.architek.oikos.accounting.application.query.GetAccountByHolderQuery;
import com.architek.oikos.accounting.application.query.GetAccountQuery;
import com.architek.oikos.accounting.application.query.ListMovementsQuery;
import com.architek.oikos.accounting.domain.valueobject.AccountId;
import com.architek.oikos.accounting.domain.valueobject.AccountType;
import com.architek.oikos.accounting.domain.valueobject.MovementId;
import com.architek.oikos.accounting.web.request.CreateAccountRequest;
import com.architek.oikos.accounting.web.request.RecordPaymentRequest;
import com.architek.oikos.accounting.web.response.AccountResponse;
import com.architek.oikos.accounting.web.response.BalanceResponse;
import com.architek.oikos.accounting.web.response.PagedMovementResponse;
import com.architek.oikos.shared.domain.pagination.PageRequest;
import com.architek.oikos.shared.domain.valueobject.EntityId;

@RestController
@RequestMapping("/accounts")
public class AccountController {

    private final CreateAccountUseCase createAccountUseCase;
    private final GetAccountUseCase getAccountUseCase;
    private final GetAccountByHolderUseCase getAccountByHolderUseCase;
    private final GetAccountBalanceUseCase getAccountBalanceUseCase;
    private final ListMovementsUseCase listMovementsUseCase;
    private final RecordPaymentUseCase recordPaymentUseCase;

    public AccountController(CreateAccountUseCase createAccountUseCase, GetAccountUseCase getAccountUseCase,
                              GetAccountByHolderUseCase getAccountByHolderUseCase,
                              GetAccountBalanceUseCase getAccountBalanceUseCase, ListMovementsUseCase listMovementsUseCase,
                              RecordPaymentUseCase recordPaymentUseCase) {
        this.createAccountUseCase = createAccountUseCase;
        this.getAccountUseCase = getAccountUseCase;
        this.getAccountByHolderUseCase = getAccountByHolderUseCase;
        this.getAccountBalanceUseCase = getAccountBalanceUseCase;
        this.listMovementsUseCase = listMovementsUseCase;
        this.recordPaymentUseCase = recordPaymentUseCase;
    }

    @PostMapping
    public ResponseEntity<Void> create(@Valid @RequestBody CreateAccountRequest request) {
        AccountId id = createAccountUseCase.create(new CreateAccountCommand(EntityId.of(request.holderId()), request.accountType()));
        return ResponseEntity.created(URI.create("/api/v1/accounts/" + id)).build();
    }

    @GetMapping
    public AccountResponse getByHolder(@RequestParam String holderId, @RequestParam AccountType accountType) {
        return AccountResponse.from(getAccountByHolderUseCase.getAccount(
                new GetAccountByHolderQuery(EntityId.of(holderId), accountType)));
    }

    @GetMapping("/{id}")
    public AccountResponse getById(@PathVariable String id) {
        return AccountResponse.from(getAccountUseCase.getAccount(new GetAccountQuery(AccountId.of(id))));
    }

    @GetMapping("/{id}/balance")
    public BalanceResponse getBalance(@PathVariable String id) {
        return BalanceResponse.from(getAccountBalanceUseCase.getBalance(new GetAccountBalanceQuery(AccountId.of(id))));
    }

    @GetMapping("/{id}/movements")
    public PagedMovementResponse listMovements(@PathVariable String id,
                                                   @RequestParam(defaultValue = "0") int page,
                                                   @RequestParam(defaultValue = "20") int size) {
        return PagedMovementResponse.from(listMovementsUseCase.listMovements(
                new ListMovementsQuery(AccountId.of(id), PageRequest.of(page, size))));
    }

    @PostMapping("/{id}/payments")
    public ResponseEntity<Void> recordPayment(@PathVariable String id, @Valid @RequestBody RecordPaymentRequest request) {
        MovementId movementId = recordPaymentUseCase.record(new RecordPaymentCommand(AccountId.of(id), request.amount(),
                request.label(), request.businessReference()));
        return ResponseEntity.created(URI.create("/api/v1/accounts/" + id + "/movements/" + movementId)).build();
    }
}
