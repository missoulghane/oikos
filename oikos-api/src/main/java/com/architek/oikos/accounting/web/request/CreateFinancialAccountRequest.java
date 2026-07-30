package com.architek.oikos.accounting.web.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import com.architek.oikos.accounting.domain.valueobject.FinancialAccountType;

public record CreateFinancialAccountRequest(@NotBlank @Size(max = 200) String name, @NotNull FinancialAccountType type,
                                             @NotBlank @Size(max = 10) String currency) {
}
