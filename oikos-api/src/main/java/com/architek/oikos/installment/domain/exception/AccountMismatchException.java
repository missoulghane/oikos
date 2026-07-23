package com.architek.oikos.installment.domain.exception;

import com.architek.oikos.shared.exception.BusinessException;

/**
 * A movement can only be allocated to an installment carried by the same account.
 */
public class AccountMismatchException extends BusinessException {

    public AccountMismatchException() {
        super("The movement and the installment do not belong to the same account");
    }
}
