package com.architek.oikos.installment.application.dto;

import java.math.BigDecimal;
import java.util.List;

public record RecordOwnerPaymentResult(PaymentView payment, List<InstallmentAllocationView> allocations,
                                        BigDecimal advanceAmount) {
}
