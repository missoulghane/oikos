package com.architek.oikos.installment.application.dto;

import java.math.BigDecimal;
import java.util.List;

public record RegularizePropertyInstallmentsResult(List<RegularizeUnitInstallmentsResult> regularizedUnits,
                                                     BigDecimal totalAmountApplied) {
}
