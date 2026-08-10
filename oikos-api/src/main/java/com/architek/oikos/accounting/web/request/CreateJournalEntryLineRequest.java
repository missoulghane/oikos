package com.architek.oikos.accounting.web.request;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import com.architek.oikos.accounting.domain.valueobject.EntryDirection;

public record CreateJournalEntryLineRequest(@NotBlank String ledgerAccountId, String auxiliaryUnitId,
                                             String auxiliaryPartyId, @NotNull EntryDirection direction,
                                             @NotNull @DecimalMin(value = "0.01") BigDecimal amount,
                                             @NotBlank String label) {
}
