package com.architek.oikos.accounting.application.command;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.architek.oikos.accounting.domain.valueobject.LedgerAccountId;
import com.architek.oikos.shared.domain.valueobject.EntityId;

/**
 * ledgerAccountId is the class-6 charge account chosen by the caller (e.g.
 * "Remunerations du personnel" or "Charges sociales") - same principle as
 * RecordSupplierPaymentCommand's charge account. STAFF_PAYABLE (the credit
 * side) is not collective (V11 seed), so there is no per-employee auxiliary
 * to carry: this engine tracks a single payroll liability, not a
 * per-employee ledger.
 */
public record RecordPayrollExpenseCommand(EntityId propertyId, LocalDate date, LedgerAccountId ledgerAccountId,
                                           BigDecimal amount, String description, EntityId createdByUserId) {
}
