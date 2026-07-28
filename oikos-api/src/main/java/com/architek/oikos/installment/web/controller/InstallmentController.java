package com.architek.oikos.installment.web.controller;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.architek.oikos.installment.application.port.in.GetInstallmentUseCase;
import com.architek.oikos.installment.application.port.in.ListInstallmentsByPropertyUseCase;
import com.architek.oikos.installment.application.port.in.ListInstallmentsByUnitUseCase;
import com.architek.oikos.installment.application.query.GetInstallmentQuery;
import com.architek.oikos.installment.application.query.ListInstallmentsByPropertyQuery;
import com.architek.oikos.installment.application.query.ListInstallmentsByUnitQuery;
import com.architek.oikos.installment.domain.valueobject.InstallmentFilter;
import com.architek.oikos.installment.domain.valueobject.InstallmentId;
import com.architek.oikos.installment.domain.valueobject.InstallmentSortField;
import com.architek.oikos.installment.domain.valueobject.InstallmentStatus;
import com.architek.oikos.installment.web.response.InstallmentResponse;
import com.architek.oikos.installment.web.response.PagedInstallmentResponse;
import com.architek.oikos.shared.domain.pagination.PageRequest;
import com.architek.oikos.shared.domain.pagination.SortDirection;
import com.architek.oikos.shared.domain.valueobject.EntityId;

@RestController
public class InstallmentController {

    private final ListInstallmentsByUnitUseCase listInstallmentsByUnitUseCase;
    private final ListInstallmentsByPropertyUseCase listInstallmentsByPropertyUseCase;
    private final GetInstallmentUseCase getInstallmentUseCase;

    public InstallmentController(ListInstallmentsByUnitUseCase listInstallmentsByUnitUseCase,
                                  ListInstallmentsByPropertyUseCase listInstallmentsByPropertyUseCase,
                                  GetInstallmentUseCase getInstallmentUseCase) {
        this.listInstallmentsByUnitUseCase = listInstallmentsByUnitUseCase;
        this.listInstallmentsByPropertyUseCase = listInstallmentsByPropertyUseCase;
        this.getInstallmentUseCase = getInstallmentUseCase;
    }

    @GetMapping("/units/{unitId}/installments")
    public List<InstallmentResponse> listByUnit(@PathVariable String unitId) {
        return listInstallmentsByUnitUseCase.listInstallments(new ListInstallmentsByUnitQuery(EntityId.of(unitId))).stream()
                .map(InstallmentResponse::from)
                .toList();
    }

    @GetMapping("/properties/{propertyId}/installments")
    public PagedInstallmentResponse listByProperty(@PathVariable String propertyId,
                                                     @RequestParam(defaultValue = "0") int page,
                                                     @RequestParam(defaultValue = "20") int size,
                                                     @RequestParam(defaultValue = "DUE_DATE") InstallmentSortField sortBy,
                                                     @RequestParam(defaultValue = "ASC") SortDirection sortDirection,
                                                     @RequestParam(required = false) Set<InstallmentStatus> status,
                                                     @RequestParam(required = false) LocalDate dueDateFrom,
                                                     @RequestParam(required = false) LocalDate dueDateTo) {
        InstallmentFilter filter = new InstallmentFilter(status, dueDateFrom, dueDateTo, sortBy, sortDirection);
        return PagedInstallmentResponse.from(listInstallmentsByPropertyUseCase.listInstallments(
                new ListInstallmentsByPropertyQuery(EntityId.of(propertyId), filter, PageRequest.of(page, size))));
    }

    @GetMapping("/installments/{id}")
    public InstallmentResponse getById(@PathVariable String id) {
        return InstallmentResponse.from(getInstallmentUseCase.getInstallment(new GetInstallmentQuery(InstallmentId.of(id))));
    }
}
