package com.architek.oikos.installment.domain.valueobject;

/**
 * Fields an installment listing can be sorted by. Both are stored columns
 * (unlike amountPaid/remainingDue/status, which are computed at read time -
 * RG011 - and therefore excluded from sorting).
 */
public enum InstallmentSortField {
    DUE_DATE,
    AMOUNT
}
