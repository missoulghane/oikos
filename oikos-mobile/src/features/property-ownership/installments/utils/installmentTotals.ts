import type { InstallmentStatus } from '@/features/property-ownership/installments/types/installment.types';
import { todayIsoDate } from '@/shared/utils/todayIsoDate';

type Unsettled = { status: InstallmentStatus };
type Dated = Unsettled & { dueDate: string };

/**
 * Not fully settled. This is what the "À régler" filter selects on: it answers
 * "is there anything left to pay on this line", whatever its date.
 * Mirrors oikos-web's installmentTotals.ts.
 */
export function isUnsettled(installment: Unsettled): boolean {
  return installment.status !== 'SETTLED';
}

/**
 * Unsettled *and* already fallen due at the reference date - what a balance is
 * made of, and deliberately a different notion from isUnsettled: an echeance
 * dated next December is not money the copropriétaire owes today.
 *
 * Compared as ISO "YYYY-MM-DD" strings: parsing a bare date into a Date shifts
 * it by a day depending on the timezone.
 */
export function isDueBy(installment: Dated, asOf: string = todayIsoDate()): boolean {
  return isUnsettled(installment) && installment.dueDate <= asOf;
}

/**
 * Unsettled but not yet fallen due: money that will be owed, and is not today.
 * Exactly the complement of isDueBy within the unsettled lines, and exactly the
 * set the list hides by default - see MyInstallmentFiltersValue.includeNotYetDue.
 */
export function isNotYetDue(installment: Dated, asOf: string = todayIsoDate()): boolean {
  return isUnsettled(installment) && !isDueBy(installment, asOf);
}

/** What is owed at the reference date: settled lines and not-yet-due ones contribute 0. */
export function outstandingTotal(
  installments: readonly (Dated & { outstandingAmount: number })[],
  asOf: string = todayIsoDate(),
): number {
  return installments
    .filter((installment) => isDueBy(installment, asOf))
    .reduce((sum, installment) => sum + installment.outstandingAmount, 0);
}
