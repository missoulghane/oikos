package com.architek.oikos.installment.domain.valueobject;

/** Spec &sect;4.1 Reglement: VIREMENT/ESPECES/CHEQUE/PRELEVEMENT. */
public enum PaymentMode {
    BANK_TRANSFER,
    CASH,
    CHECK,
    DIRECT_DEBIT
}
