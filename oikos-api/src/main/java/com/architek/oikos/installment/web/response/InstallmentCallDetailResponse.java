package com.architek.oikos.installment.web.response;

import java.time.LocalDate;
import java.util.List;

import com.architek.oikos.installment.application.dto.InstallmentCallDetailView;

public record InstallmentCallDetailResponse(String id, String propertyId, String period, LocalDate dueDate,
                                               String status, List<InstallmentResponse> installments) {

    public static InstallmentCallDetailResponse from(InstallmentCallDetailView view) {
        return new InstallmentCallDetailResponse(
                view.installmentCall().id().toString(),
                view.installmentCall().propertyId().toString(),
                view.installmentCall().period().toString(),
                view.installmentCall().dueDate(),
                view.installmentCall().status().name(),
                view.installments().stream().map(InstallmentResponse::from).toList());
    }
}
