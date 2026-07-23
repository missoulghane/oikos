package com.architek.oikos.installment.web.response;

import java.time.LocalDate;
import java.util.List;

import com.architek.oikos.installment.application.dto.GenerateInstallmentCallResult;

public record GenerateInstallmentCallResponse(String id, String propertyId, String period, LocalDate dueDate,
                                                 List<String> chargedUnitIds, List<String> skippedUnitIds) {

    public static GenerateInstallmentCallResponse from(GenerateInstallmentCallResult result) {
        return new GenerateInstallmentCallResponse(
                result.installmentCall().id().toString(),
                result.installmentCall().propertyId().toString(),
                result.installmentCall().period().toString(),
                result.installmentCall().dueDate(),
                result.chargedUnitIds().stream().map(Object::toString).toList(),
                result.skippedUnitIds().stream().map(Object::toString).toList());
    }
}
