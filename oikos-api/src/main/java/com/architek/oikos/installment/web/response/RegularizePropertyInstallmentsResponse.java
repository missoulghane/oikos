package com.architek.oikos.installment.web.response;

import java.math.BigDecimal;
import java.util.List;

import com.architek.oikos.installment.application.dto.RegularizePropertyInstallmentsResult;

public record RegularizePropertyInstallmentsResponse(int unitsRegularized, BigDecimal totalAmountApplied,
                                                       List<RegularizeUnitInstallmentsResponse> regularizedUnits) {

    public static RegularizePropertyInstallmentsResponse from(RegularizePropertyInstallmentsResult result) {
        List<RegularizeUnitInstallmentsResponse> regularizedUnits = result.regularizedUnits().stream()
                .map(RegularizeUnitInstallmentsResponse::from)
                .toList();
        return new RegularizePropertyInstallmentsResponse(regularizedUnits.size(), result.totalAmountApplied(),
                regularizedUnits);
    }
}
