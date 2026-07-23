package com.architek.oikos.accounting.application.dto;

import java.math.BigDecimal;

import com.architek.oikos.accounting.domain.valueobject.AccountId;

public record BalanceView(AccountId accountId, BigDecimal balance) {
}
