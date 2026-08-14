import type { InstallmentStatus } from '@/features/property-mngt/installments/types/installment.types';

/**
 * "À régler" is everything not fully settled - a partially settled echeance
 * still owes its outstanding part, so it counts as due. Kept as one predicate
 * because the badge total, the "À régler" filter and the dashboard's "Solde à
 * régler" must never disagree on what counts as owed.
 */
export function isDue(installment: { status: InstallmentStatus }): boolean {
  return installment.status !== 'SETTLED';
}

/** Sum of what is still owed across the given echeances (settled ones contribute 0). */
export function outstandingTotal(installments: readonly { status: InstallmentStatus; outstandingAmount: number }[]): number {
  return installments.filter(isDue).reduce((sum, installment) => sum + installment.outstandingAmount, 0);
}
