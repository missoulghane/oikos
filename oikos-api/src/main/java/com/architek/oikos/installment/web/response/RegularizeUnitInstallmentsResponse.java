package com.architek.oikos.installment.web.response;

import java.math.BigDecimal;
import java.util.List;

import com.architek.oikos.installment.application.dto.RegularizeUnitInstallmentsResult;

public record RegularizeUnitInstallmentsResponse(String unitId, String journalEntryId, BigDecimal amountApplied,
                                                  List<InstallmentAllocationResponse> allocations) {

    public static RegularizeUnitInstallmentsResponse from(RegularizeUnitInstallmentsResult result) {
        List<InstallmentAllocationResponse> allocations = result.allocations().stream()
                .map(InstallmentAllocationResponse::from)
                .toList();
        return new RegularizeUnitInstallmentsResponse(result.unitId().toString(), result.journalEntryId().toString(),
                result.amountApplied(), allocations);
    }
}
