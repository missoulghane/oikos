package com.architek.oikos.installment.application.dto;

import java.util.List;

public record InstallmentCallDetailView(InstallmentCallView installmentCall, List<InstallmentView> installments) {
}
