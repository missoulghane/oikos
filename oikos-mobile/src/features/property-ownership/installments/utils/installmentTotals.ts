import type { InstallmentStatus } from '@/features/property-ownership/installments/types/installment.types';

/**
 * "À régler" is everything not fully settled - a partially settled echeance
 * still owes its outstanding part. Mirrors oikos-web's installmentTotals.ts so
 * the badge total and the "À régler" filter agree across both clients.
 */
export function isDue(installment: { status: InstallmentStatus }): boolean {
  return installment.status !== 'SETTLED';
}

export function outstandingTotal(
  installments: readonly { status: InstallmentStatus; outstandingAmount: number }[],
): number {
  return installments.filter(isDue).reduce((sum, installment) => sum + installment.outstandingAmount, 0);
}
