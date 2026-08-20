package com.architek.oikos.accounting.web.controller;

import java.net.URI;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import com.architek.oikos.accounting.application.command.CloseAccountingExerciseCommand;
import com.architek.oikos.accounting.application.command.OpenAccountingExerciseCommand;
import com.architek.oikos.accounting.application.port.in.CloseAccountingExerciseUseCase;
import com.architek.oikos.accounting.application.port.in.GetOpenAccountingExerciseUseCase;
import com.architek.oikos.accounting.application.port.in.OpenAccountingExerciseUseCase;
import com.architek.oikos.accounting.application.query.GetOpenAccountingExerciseQuery;
import com.architek.oikos.accounting.domain.valueobject.AccountingExerciseId;
import com.architek.oikos.accounting.web.request.OpenAccountingExerciseRequest;
import com.architek.oikos.accounting.web.response.AccountingExerciseResponse;
import com.architek.oikos.accounting.web.response.ExerciseClosingResponse;
import com.architek.oikos.shared.domain.valueobject.EntityId;

@RestController
public class AccountingExerciseController {

    private final OpenAccountingExerciseUseCase openAccountingExerciseUseCase;
    private final GetOpenAccountingExerciseUseCase getOpenAccountingExerciseUseCase;
    private final CloseAccountingExerciseUseCase closeAccountingExerciseUseCase;

    public AccountingExerciseController(OpenAccountingExerciseUseCase openAccountingExerciseUseCase,
                                         GetOpenAccountingExerciseUseCase getOpenAccountingExerciseUseCase,
                                         CloseAccountingExerciseUseCase closeAccountingExerciseUseCase) {
        this.openAccountingExerciseUseCase = openAccountingExerciseUseCase;
        this.getOpenAccountingExerciseUseCase = getOpenAccountingExerciseUseCase;
        this.closeAccountingExerciseUseCase = closeAccountingExerciseUseCase;
    }

    @PreAuthorize("@propertyAccess.canWriteAccounting(authentication, #propertyId)")
    @PostMapping("/properties/{propertyId}/accounting/exercises")
    public ResponseEntity<Void> open(@PathVariable String propertyId,
                                      @Valid @RequestBody OpenAccountingExerciseRequest request) {
        AccountingExerciseId id = openAccountingExerciseUseCase.open(new OpenAccountingExerciseCommand(
                EntityId.of(propertyId), request.label(), request.startDate(), request.endDate(), request.comment()));
        return ResponseEntity.created(URI.create("/api/v1/properties/" + propertyId + "/accounting/exercises/" + id))
                .build();
    }

    /**
     * La clôture annuelle. Elle porte sur l'exercice ouvert de la copropriété -
     * il n'y en a qu'un - d'où l'absence d'identifiant dans le chemin.
     */
    @PreAuthorize("@propertyAccess.managesProperty(authentication, #propertyId)")
    @PostMapping("/properties/{propertyId}/accounting/exercises/close")
    public ExerciseClosingResponse close(@PathVariable String propertyId, Authentication authentication) {
        return ExerciseClosingResponse.from(closeAccountingExerciseUseCase.close(
                new CloseAccountingExerciseCommand(EntityId.of(propertyId), EntityId.of(authentication.getName()))));
    }

    @PreAuthorize("@propertyAccess.canReadAccounting(authentication, #propertyId)")
    @GetMapping("/properties/{propertyId}/accounting/exercises/open")
    public AccountingExerciseResponse getOpen(@PathVariable String propertyId) {
        return AccountingExerciseResponse.from(
                getOpenAccountingExerciseUseCase.get(new GetOpenAccountingExerciseQuery(EntityId.of(propertyId))));
    }
}
