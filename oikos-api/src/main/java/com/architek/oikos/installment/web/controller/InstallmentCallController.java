package com.architek.oikos.installment.web.controller;

import java.time.YearMonth;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import com.architek.oikos.installment.application.command.InstallmentCallLine;
import com.architek.oikos.installment.application.command.GenerateInstallmentCallCommand;
import com.architek.oikos.installment.application.command.RecordInstallmentCallCommand;
import com.architek.oikos.installment.application.port.in.GenerateInstallmentCallUseCase;
import com.architek.oikos.installment.application.port.in.GetInstallmentCallUseCase;
import com.architek.oikos.installment.application.port.in.ListInstallmentCallsByPropertyUseCase;
import com.architek.oikos.installment.application.port.in.RecordInstallmentCallUseCase;
import com.architek.oikos.installment.application.query.GetInstallmentCallQuery;
import com.architek.oikos.installment.application.query.ListInstallmentCallsByPropertyQuery;
import com.architek.oikos.installment.domain.valueobject.InstallmentCallId;
import com.architek.oikos.installment.domain.valueobject.InstallmentId;
import com.architek.oikos.installment.web.request.InstallmentCallLineRequest;
import com.architek.oikos.installment.web.request.GenerateInstallmentCallRequest;
import com.architek.oikos.installment.web.request.RecordInstallmentCallRequest;
import com.architek.oikos.installment.web.response.InstallmentCallDetailResponse;
import com.architek.oikos.installment.web.response.InstallmentCallResponse;
import com.architek.oikos.installment.web.response.GenerateInstallmentCallResponse;
import com.architek.oikos.installment.web.response.PagedInstallmentCallResponse;
import com.architek.oikos.shared.domain.pagination.PageRequest;
import com.architek.oikos.shared.domain.valueobject.EntityId;

@RestController
public class InstallmentCallController {

    private final RecordInstallmentCallUseCase recordInstallmentCallUseCase;
    private final GenerateInstallmentCallUseCase generateInstallmentCallUseCase;
    private final ListInstallmentCallsByPropertyUseCase listInstallmentCallsByPropertyUseCase;
    private final GetInstallmentCallUseCase getInstallmentCallUseCase;

    public InstallmentCallController(RecordInstallmentCallUseCase recordInstallmentCallUseCase,
                                     GenerateInstallmentCallUseCase generateInstallmentCallUseCase,
                                     ListInstallmentCallsByPropertyUseCase listInstallmentCallsByPropertyUseCase,
                                     GetInstallmentCallUseCase getInstallmentCallUseCase) {
        this.recordInstallmentCallUseCase = recordInstallmentCallUseCase;
        this.generateInstallmentCallUseCase = generateInstallmentCallUseCase;
        this.listInstallmentCallsByPropertyUseCase = listInstallmentCallsByPropertyUseCase;
        this.getInstallmentCallUseCase = getInstallmentCallUseCase;
    }

    // No propertyId in the path here (the request body carries arbitrary unitIds
    // potentially spanning properties) - restricted to ADMIN until per-line
    // property validation is built; a MANAGER should use the property-scoped
    // /properties/{propertyId}/installment-calls "generate" endpoint below instead.
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @PostMapping("/installment-calls")
    public ResponseEntity<InstallmentCallResponse> create(@Valid @RequestBody RecordInstallmentCallRequest request) {
        List<InstallmentId> installmentIds = recordInstallmentCallUseCase.record(new RecordInstallmentCallCommand(
                request.dueDate(), request.lines().stream().map(InstallmentCallController::toLine).toList()));
        return ResponseEntity.status(HttpStatus.CREATED).body(InstallmentCallResponse.from(installmentIds));
    }

    @PreAuthorize("@propertyAccess.canWriteInstallmentCall(authentication, #propertyId)")
    @PostMapping("/properties/{propertyId}/installment-calls")
    public ResponseEntity<GenerateInstallmentCallResponse> generate(@PathVariable String propertyId,
                                                                     @Valid @RequestBody GenerateInstallmentCallRequest request) {
        GenerateInstallmentCallResponse response = GenerateInstallmentCallResponse.from(generateInstallmentCallUseCase.generate(
                new GenerateInstallmentCallCommand(EntityId.of(propertyId), YearMonth.parse(request.period()), request.dueDate())));
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

    private static InstallmentCallLine toLine(InstallmentCallLineRequest request) {
        return new InstallmentCallLine(EntityId.of(request.unitId()), request.amount());
    }
}
