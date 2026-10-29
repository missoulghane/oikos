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
import com.architek.oikos.accounting.application.command.CloseAccountingExerciseCommand;
import com.architek.oikos.accounting.application.command.OpenAccountingExerciseCommand;
import com.architek.oikos.accounting.application.port.in.CloseAccountingExerciseUseCase;
import com.architek.oikos.accounting.application.port.in.ListAccountingExercisesUseCase;
import com.architek.oikos.accounting.application.port.in.OpenAccountingExerciseUseCase;
import com.architek.oikos.accounting.application.query.ListAccountingExercisesQuery;
import com.architek.oikos.accounting.domain.valueobject.AccountingExerciseId;
import com.architek.oikos.accounting.web.request.OpenAccountingExerciseRequest;
import com.architek.oikos.accounting.web.response.AccountingExerciseResponse;
import com.architek.oikos.accounting.web.response.ExerciseClosingResponse;
import com.architek.oikos.shared.domain.valueobject.EntityId;

@RestController
public class AccountingExerciseController {

    private final OpenAccountingExerciseUseCase openAccountingExerciseUseCase;
    private final ListAccountingExercisesUseCase listAccountingExercisesUseCase;
    private final CloseAccountingExerciseUseCase closeAccountingExerciseUseCase;

    public AccountingExerciseController(OpenAccountingExerciseUseCase openAccountingExerciseUseCase,
                                         ListAccountingExercisesUseCase listAccountingExercisesUseCase,
                                         CloseAccountingExerciseUseCase closeAccountingExerciseUseCase) {
        this.openAccountingExerciseUseCase = openAccountingExerciseUseCase;
        this.listAccountingExercisesUseCase = listAccountingExercisesUseCase;
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

    /** Tous les exercices de la copropriété, du plus ancien au plus récent - jusqu'à deux peuvent être ouverts. */
    @PreAuthorize("@propertyAccess.canReadAccounting(authentication, #propertyId)")
    @GetMapping("/properties/{propertyId}/accounting/exercises")
    public List<AccountingExerciseResponse> list(@PathVariable String propertyId) {
        return listAccountingExercisesUseCase.list(new ListAccountingExercisesQuery(EntityId.of(propertyId))).stream()
                .map(AccountingExerciseResponse::from)
                .toList();
    }

    /**
     * La clôture annuelle. Deux exercices pouvant être ouverts à la fois, celui
     * qu'on clôture est désigné - et ce doit être le plus ancien des deux.
     */
    @PreAuthorize("@propertyAccess.managesProperty(authentication, #propertyId)")
    @PostMapping("/properties/{propertyId}/accounting/exercises/{exerciseId}/close")
    public ExerciseClosingResponse close(@PathVariable String propertyId, @PathVariable String exerciseId,
                                          Authentication authentication) {
        return ExerciseClosingResponse.from(closeAccountingExerciseUseCase.close(new CloseAccountingExerciseCommand(
                EntityId.of(propertyId), AccountingExerciseId.of(exerciseId), EntityId.of(authentication.getName()))));
    }
}
