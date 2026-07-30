package com.architek.oikos.accounting.web.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.architek.oikos.accounting.application.command.ValidateBulkLettrageCommand;
import com.architek.oikos.accounting.application.port.in.ListPendingLettragesByPropertyUseCase;
import com.architek.oikos.accounting.application.port.in.ValidateBulkLettrageUseCase;
import com.architek.oikos.accounting.application.query.ListPendingLettragesByPropertyQuery;
import com.architek.oikos.accounting.web.request.ValidateBulkLettrageRequest;
import com.architek.oikos.accounting.web.response.PendingLettrageResponse;
import com.architek.oikos.shared.domain.valueobject.EntityId;

/** Property-wide lettrage review/validation (spec: "les deux" - unitaire sur UnitAccountController, en masse ici). */
@RestController
public class LettrageController {

    private final ListPendingLettragesByPropertyUseCase listPendingLettragesByPropertyUseCase;
    private final ValidateBulkLettrageUseCase validateBulkLettrageUseCase;

    public LettrageController(ListPendingLettragesByPropertyUseCase listPendingLettragesByPropertyUseCase,
                               ValidateBulkLettrageUseCase validateBulkLettrageUseCase) {
        this.listPendingLettragesByPropertyUseCase = listPendingLettragesByPropertyUseCase;
        this.validateBulkLettrageUseCase = validateBulkLettrageUseCase;
    }

    @PreAuthorize("@propertyAccess.canReadAccounting(authentication, #propertyId)")
    @GetMapping("/properties/{propertyId}/accounting/lettrage/pending")
    public List<PendingLettrageResponse> listPending(@PathVariable String propertyId) {
        return listPendingLettragesByPropertyUseCase
                .list(new ListPendingLettragesByPropertyQuery(EntityId.of(propertyId))).stream()
                .map(PendingLettrageResponse::from)
                .toList();
    }

    @PreAuthorize("@propertyAccess.canWriteAccounting(authentication, #propertyId)")
    @PostMapping("/properties/{propertyId}/accounting/lettrage/validate")
    public ResponseEntity<Void> validateBulk(@PathVariable String propertyId,
                                              @RequestBody(required = false) ValidateBulkLettrageRequest request,
                                              Authentication authentication) {
        List<EntityId> unitIds = request == null || request.unitIds() == null
                ? null
                : request.unitIds().stream().map(EntityId::of).toList();
        validateBulkLettrageUseCase.validate(
                new ValidateBulkLettrageCommand(EntityId.of(propertyId), unitIds, currentUserId(authentication)));
        return ResponseEntity.noContent().build();
    }

    private static EntityId currentUserId(Authentication authentication) {
        return EntityId.of(authentication.getName());
    }
}
