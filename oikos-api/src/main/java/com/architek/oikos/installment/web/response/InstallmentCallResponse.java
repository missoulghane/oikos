package com.architek.oikos.installment.web.response;

import java.util.List;

import com.architek.oikos.installment.domain.valueobject.InstallmentId;

public record InstallmentCallResponse(List<String> installmentIds) {

    public static InstallmentCallResponse from(List<InstallmentId> ids) {
        return new InstallmentCallResponse(ids.stream().map(InstallmentId::toString).toList());
    }
}
