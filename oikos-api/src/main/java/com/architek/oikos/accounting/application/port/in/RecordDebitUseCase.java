package com.architek.oikos.accounting.application.port.in;

import com.architek.oikos.accounting.application.command.RecordDebitCommand;
import com.architek.oikos.accounting.domain.valueobject.MovementId;

/**
 * Records a debit movement against an account, applying its balance/mirror
 * side effects (RG010/RG010bis) - the ledger half of raising a due amount.
 * Exposed as a port-in specifically so the installment module (which owns
 * the "due amount" concept, e.g. GenerateInstallmentCallUseCase) can trigger
 * it without depending on accounting's repositories directly (rule 4).
 */
public interface RecordDebitUseCase {

    MovementId record(RecordDebitCommand command);
}
