package com.architek.oikos.installment.web.controller;

import java.net.URI;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import com.architek.oikos.installment.application.command.AllocatePaymentCommand;
import com.architek.oikos.installment.application.command.DeallocateCommand;
import com.architek.oikos.installment.application.port.in.AllocatePaymentUseCase;
import com.architek.oikos.installment.application.port.in.DeallocateUseCase;
import com.architek.oikos.installment.application.port.in.ListAllocationsByMovementUseCase;
import com.architek.oikos.installment.application.query.ListAllocationsByMovementQuery;
import com.architek.oikos.installment.domain.valueobject.AllocationId;
import com.architek.oikos.installment.domain.valueobject.InstallmentId;
import com.architek.oikos.shared.domain.valueobject.EntityId;
import com.architek.oikos.installment.web.request.AllocatePaymentRequest;
import com.architek.oikos.installment.web.response.AllocationResponse;

@RestController
public class AllocationController {

    private final AllocatePaymentUseCase allocatePaymentUseCase;
    private final DeallocateUseCase deallocateUseCase;
    private final ListAllocationsByMovementUseCase listAllocationsByMovementUseCase;

    public AllocationController(AllocatePaymentUseCase allocatePaymentUseCase, DeallocateUseCase deallocateUseCase,
                                 ListAllocationsByMovementUseCase listAllocationsByMovementUseCase) {
        this.allocatePaymentUseCase = allocatePaymentUseCase;
        this.deallocateUseCase = deallocateUseCase;
        this.listAllocationsByMovementUseCase = listAllocationsByMovementUseCase;
    }

    @PostMapping("/allocations")
    public ResponseEntity<Void> allocate(@Valid @RequestBody AllocatePaymentRequest request) {
        AllocationId id = allocatePaymentUseCase.allocate(new AllocatePaymentCommand(
                EntityId.of(request.movementId()), InstallmentId.of(request.installmentId()), request.amount()));
        return ResponseEntity.created(URI.create("/api/v1/allocations/" + id)).build();
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping("/allocations/{id}")
    public void deallocate(@PathVariable String id) {
        deallocateUseCase.deallocate(new DeallocateCommand(AllocationId.of(id)));
    }

    @GetMapping("/movements/{id}/allocations")
    public List<AllocationResponse> listByMovement(@PathVariable String id) {
        return listAllocationsByMovementUseCase.listAllocations(new ListAllocationsByMovementQuery(EntityId.of(id)))
                .stream()
                .map(AllocationResponse::from)
                .toList();
    }
}
