package com.architek.oikos.installment.web.controller;

import java.time.YearMonth;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import com.architek.oikos.installment.application.command.CancelInstallmentCallCommand;
import com.architek.oikos.installment.application.command.GenerateInstallmentCallCommand;
import com.architek.oikos.installment.application.port.in.CancelInstallmentCallUseCase;
import com.architek.oikos.installment.application.port.in.GenerateInstallmentCallUseCase;
import com.architek.oikos.installment.application.port.in.GetInstallmentCallUseCase;
import com.architek.oikos.installment.application.port.in.ListInstallmentCallsByPropertyUseCase;
import com.architek.oikos.installment.application.query.GetInstallmentCallQuery;
import com.architek.oikos.installment.application.query.ListInstallmentCallsByPropertyQuery;
import com.architek.oikos.installment.domain.valueobject.InstallmentCallId;
import com.architek.oikos.installment.web.request.GenerateInstallmentCallRequest;
import com.architek.oikos.installment.web.response.GenerateInstallmentCallResponse;
import com.architek.oikos.installment.web.response.InstallmentCallDetailResponse;
import com.architek.oikos.installment.web.response.PagedInstallmentCallResponse;
import com.architek.oikos.shared.domain.pagination.PageRequest;
import com.architek.oikos.shared.domain.valueobject.EntityId;

@RestController
public class InstallmentCallController {

    private final GenerateInstallmentCallUseCase generateInstallmentCallUseCase;
    private final ListInstallmentCallsByPropertyUseCase listInstallmentCallsByPropertyUseCase;
    private final GetInstallmentCallUseCase getInstallmentCallUseCase;
    private final CancelInstallmentCallUseCase cancelInstallmentCallUseCase;

    public InstallmentCallController(GenerateInstallmentCallUseCase generateInstallmentCallUseCase,
                                     ListInstallmentCallsByPropertyUseCase listInstallmentCallsByPropertyUseCase,
                                     GetInstallmentCallUseCase getInstallmentCallUseCase,
                                     CancelInstallmentCallUseCase cancelInstallmentCallUseCase) {
        this.generateInstallmentCallUseCase = generateInstallmentCallUseCase;
        this.listInstallmentCallsByPropertyUseCase = listInstallmentCallsByPropertyUseCase;
        this.getInstallmentCallUseCase = getInstallmentCallUseCase;
        this.cancelInstallmentCallUseCase = cancelInstallmentCallUseCase;
    }

    @PreAuthorize("@propertyAccess.canWriteInstallmentCall(authentication, #propertyId)")
    @PostMapping("/properties/{propertyId}/installment-calls")
    public ResponseEntity<GenerateInstallmentCallResponse> generate(@PathVariable String propertyId,
                                                                     @Valid @RequestBody GenerateInstallmentCallRequest request,
                                                                     Authentication authentication) {
        GenerateInstallmentCallResponse response = GenerateInstallmentCallResponse.from(generateInstallmentCallUseCase.generate(
                new GenerateInstallmentCallCommand(EntityId.of(propertyId), YearMonth.parse(request.period()),
                        request.dueDate(), EntityId.of(authentication.getName()))));
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PreAuthorize("@propertyAccess.managesProperty(authentication, #propertyId)")
    @GetMapping("/properties/{propertyId}/installment-calls")
    public PagedInstallmentCallResponse listByProperty(@PathVariable String propertyId,
                                                        @RequestParam(defaultValue = "0") int page,
                                                        @RequestParam(defaultValue = "20") int size) {
        return PagedInstallmentCallResponse.from(listInstallmentCallsByPropertyUseCase.listInstallmentCalls(
                new ListInstallmentCallsByPropertyQuery(EntityId.of(propertyId), PageRequest.of(page, size))));
    }

    @PreAuthorize("@propertyAccess.managesInstallmentCall(authentication, #id)")
    @GetMapping("/installment-calls/{id}")
    public InstallmentCallDetailResponse getById(@PathVariable String id) {
        return InstallmentCallDetailResponse.from(
                getInstallmentCallUseCase.getInstallmentCall(new GetInstallmentCallQuery(InstallmentCallId.of(id))));
    }

    /**
     * Annule un appel : son écriture VT est contre-passée et il reste listé,
     * au statut CANCELLED. Un appel n'est jamais supprimé - il a été comptabilisé.
     */
    @PreAuthorize("@propertyAccess.managesInstallmentCall(authentication, #id)")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PostMapping("/installment-calls/{id}/cancellation")
    public void cancel(@PathVariable String id, Authentication authentication) {
        cancelInstallmentCallUseCase.cancel(new CancelInstallmentCallCommand(InstallmentCallId.of(id),
                EntityId.of(authentication.getName())));
    }
}
