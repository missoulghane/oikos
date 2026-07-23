package com.architek.oikos.accounting.web.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import com.architek.oikos.accounting.domain.valueobject.AccountType;

public record CreateAccountRequest(@NotBlank String holderId, @NotNull AccountType accountType) {
}
