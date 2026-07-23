package com.architek.oikos.installment.application.query;

import com.architek.oikos.installment.domain.valueobject.InstallmentId;

public record ListAllocationsByInstallmentQuery(InstallmentId installmentId) {
}
