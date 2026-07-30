package com.architek.oikos.accounting.domain.exception;

import java.math.BigDecimal;

import com.architek.oikos.accounting.domain.valueobject.FinancialAccountId;
import com.architek.oikos.shared.exception.BusinessException;

/** A transfer's amount cannot exceed the source financial account's balance. */
public class InsufficientFundsException extends BusinessException {

    public InsufficientFundsException(FinancialAccountId financialAccountId, BigDecimal balance, BigDecimal requested) {
        super("Financial account " + financialAccountId + " has insufficient funds: balance " + balance
                + ", requested " + requested);
    }
}
