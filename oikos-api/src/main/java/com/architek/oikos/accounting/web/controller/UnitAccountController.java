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
import com.architek.oikos.accounting.application.command.RecordOwnerPaymentCommand;
import com.architek.oikos.accounting.application.command.RecordUnitAccountRegularizationCommand;
import com.architek.oikos.accounting.application.command.ValidateUnitLettrageCommand;
import com.architek.oikos.accounting.application.port.in.GetUnitAccountUseCase;
import com.architek.oikos.accounting.application.port.in.GetUnitLettrageProposalUseCase;
import com.architek.oikos.accounting.application.port.in.ListUnitAccountMovementsUseCase;
import com.architek.oikos.accounting.application.port.in.RecordOwnerPaymentUseCase;
import com.architek.oikos.accounting.application.port.in.RecordUnitAccountRegularizationUseCase;
import com.architek.oikos.accounting.application.port.in.ValidateUnitLettrageUseCase;
import com.architek.oikos.accounting.application.query.GetUnitAccountQuery;
import com.architek.oikos.accounting.application.query.GetUnitLettrageProposalQuery;
import com.architek.oikos.accounting.application.query.ListUnitAccountMovementsQuery;
import com.architek.oikos.accounting.domain.valueobject.FinancialAccountId;
import com.architek.oikos.accounting.domain.valueobject.UnitAccountMovementId;
import com.architek.oikos.accounting.web.request.RecordOwnerPaymentRequest;
import com.architek.oikos.accounting.web.request.RecordUnitAccountRegularizationRequest;
import com.architek.oikos.accounting.web.response.LettrageProposalResponse;
import com.architek.oikos.accounting.web.response.PagedUnitAccountMovementResponse;
import com.architek.oikos.accounting.web.response.UnitAccountResponse;
import com.architek.oikos.shared.domain.pagination.PageRequest;
import com.architek.oikos.shared.domain.valueobject.EntityId;

@RestController
public class UnitAccountController {

    private final GetUnitAccountUseCase getUnitAccountUseCase;
    private final ListUnitAccountMovementsUseCase listUnitAccountMovementsUseCase;
    private final RecordOwnerPaymentUseCase recordOwnerPaymentUseCase;
    private final RecordUnitAccountRegularizationUseCase recordUnitAccountRegularizationUseCase;
    private final GetUnitLettrageProposalUseCase getUnitLettrageProposalUseCase;
    private final ValidateUnitLettrageUseCase validateUnitLettrageUseCase;

    public UnitAccountController(GetUnitAccountUseCase getUnitAccountUseCase,
                                  ListUnitAccountMovementsUseCase listUnitAccountMovementsUseCase,
                                  RecordOwnerPaymentUseCase recordOwnerPaymentUseCase,
                                  RecordUnitAccountRegularizationUseCase recordUnitAccountRegularizationUseCase,
                                  GetUnitLettrageProposalUseCase getUnitLettrageProposalUseCase,
                                  ValidateUnitLettrageUseCase validateUnitLettrageUseCase) {
        this.getUnitAccountUseCase = getUnitAccountUseCase;
        this.listUnitAccountMovementsUseCase = listUnitAccountMovementsUseCase;
        this.recordOwnerPaymentUseCase = recordOwnerPaymentUseCase;
        this.recordUnitAccountRegularizationUseCase = recordUnitAccountRegularizationUseCase;
        this.getUnitLettrageProposalUseCase = getUnitLettrageProposalUseCase;
        this.validateUnitLettrageUseCase = validateUnitLettrageUseCase;
    }

    @PreAuthorize("@propertyAccess.managesUnit(authentication, #unitId) or @propertyAccess.ownsUnit(authentication, #unitId)")
    @GetMapping("/units/{unitId}/account")
    public UnitAccountResponse get(@PathVariable String unitId) {
        return UnitAccountResponse.from(getUnitAccountUseCase.get(new GetUnitAccountQuery(EntityId.of(unitId))));
    }

    @PreAuthorize("@propertyAccess.managesUnit(authentication, #unitId) or @propertyAccess.ownsUnit(authentication, #unitId)")
    @GetMapping("/units/{unitId}/account/movements")
    public PagedUnitAccountMovementResponse listMovements(@PathVariable String unitId,
                                                           @RequestParam(defaultValue = "0") int page,
                                                           @RequestParam(defaultValue = "20") int size) {
        return PagedUnitAccountMovementResponse.from(listUnitAccountMovementsUseCase.list(
                new ListUnitAccountMovementsQuery(EntityId.of(unitId), PageRequest.of(page, size))));
    }

    @PreAuthorize("@propertyAccess.canWriteAccounting(authentication, #propertyId)")
    @PostMapping("/properties/{propertyId}/accounting/units/{unitId}/payments")
    public ResponseEntity<Void> recordPayment(@PathVariable String propertyId, @PathVariable String unitId,
                                               @Valid @RequestBody RecordOwnerPaymentRequest request,
                                               Authentication authentication) {
        UnitAccountMovementId id = recordOwnerPaymentUseCase.record(new RecordOwnerPaymentCommand(
                EntityId.of(propertyId), EntityId.of(unitId), FinancialAccountId.of(request.financialAccountId()),
                request.amount(), request.date(), request.label(), currentUserId(authentication)));
        return ResponseEntity
                .created(URI.create("/api/v1/units/" + unitId + "/account/movements/" + id))
                .build();
    }

    @PreAuthorize("@propertyAccess.canWriteAccounting(authentication, #propertyId)")
    @PostMapping("/properties/{propertyId}/accounting/units/{unitId}/regularizations")
    public ResponseEntity<Void> recordRegularization(@PathVariable String propertyId, @PathVariable String unitId,
                                                      @Valid @RequestBody RecordUnitAccountRegularizationRequest request) {
        UnitAccountMovementId id = recordUnitAccountRegularizationUseCase.record(
                new RecordUnitAccountRegularizationCommand(EntityId.of(unitId), request.amount(), request.direction(),
                        request.label(), request.reason()));
        return ResponseEntity
                .created(URI.create("/api/v1/units/" + unitId + "/account/movements/" + id))
                .build();
    }

    @PreAuthorize("@propertyAccess.canReadAccounting(authentication, #propertyId)")
    @GetMapping("/properties/{propertyId}/accounting/units/{unitId}/lettrage-proposal")
    public LettrageProposalResponse getLettrageProposal(@PathVariable String propertyId, @PathVariable String unitId) {
        return LettrageProposalResponse
                .from(getUnitLettrageProposalUseCase.get(new GetUnitLettrageProposalQuery(EntityId.of(unitId))));
    }

    @PreAuthorize("@propertyAccess.canWriteAccounting(authentication, #propertyId)")
    @PostMapping("/properties/{propertyId}/accounting/units/{unitId}/lettrage/validate")
    public ResponseEntity<Void> validateLettrage(@PathVariable String propertyId, @PathVariable String unitId,
                                                  Authentication authentication) {
        validateUnitLettrageUseCase
                .validate(new ValidateUnitLettrageCommand(EntityId.of(unitId), currentUserId(authentication)));
        return ResponseEntity.noContent().build();
    }

    private static EntityId currentUserId(Authentication authentication) {
        return EntityId.of(authentication.getName());
    }
}
