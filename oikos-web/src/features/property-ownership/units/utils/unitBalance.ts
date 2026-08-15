import { outstandingTotal } from '@/features/property-ownership/installments/utils/installmentTotals';
import type { InstallmentStatus } from '@/features/property-mngt/installments/types/installment.types';

type BalanceInput = { unitId: string; status: InstallmentStatus; dueDate: string; outstandingAmount: number };

/**
 * What a lot still owes, derived from its own echeances.
 *
 * Deliberately not called a "solde": the accounting balance of a lot lives on
 * its UNIT_RECEIVABLE ledger account, which is gated behind ACCOUNTING_READ and
 * unreadable by a plain owner. What an owner can derive is debt only - it never
 * goes negative, so it cannot express an avance/credit. Payments must not be
 * subtracted here: a payment already reduces `outstandingAmount` through its
 * allocation, so netting them again would double-count.
 *
 * Only counts what has fallen due at the reference date - see isDueBy.
 */
export function unitOutstanding(installments: readonly BalanceInput[], unitId: string, asOf?: string): number {
  return outstandingTotal(installments.filter((installment) => installment.unitId === unitId), asOf);
}

/**
 * Same palette as the accounting module's getTreasuryBalanceColorClass, with the
 * polarity a copropriétaire expects: there, a balance is cash on hand and more
 * is better; here it is a debt, so any amount owed is the bad case. Reusing that
 * helper unchanged would paint a 5 000 MAD debt green.
 */
export function getOutstandingColorClass(outstanding: number): string {
  if (outstanding > 0) {
    return 'text-error-600 dark:text-error-400';
  }
  return 'text-success-600 dark:text-success-500';
}
