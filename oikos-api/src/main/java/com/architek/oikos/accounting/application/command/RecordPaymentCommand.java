package com.architek.oikos.accounting.application.command;

import java.math.BigDecimal;

import com.architek.oikos.accounting.domain.valueobject.AccountId;

public record RecordPaymentCommand(AccountId accountId, BigDecimal amount, String label, String businessReference) {
}
