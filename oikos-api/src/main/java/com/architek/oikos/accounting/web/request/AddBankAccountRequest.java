package com.architek.oikos.accounting.web.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * bankAccountNumber is the bank's own reference (RIB, IBAN...) and is
 * optional: the wizard lets a volunteer syndic declare the account now and
 * fill in its details later. Free text on purpose - see
 * LedgerAccount.withBankAccountNumber.
 */
public record AddBankAccountRequest(
        @NotBlank @Size(max = 200) String label,
        @Size(max = 64) String bankAccountNumber) {
}
