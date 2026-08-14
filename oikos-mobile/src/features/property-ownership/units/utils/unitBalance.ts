import { outstandingTotal } from '@/features/property-ownership/installments/utils/installmentTotals';
import type { InstallmentStatus } from '@/features/property-ownership/installments/types/installment.types';
import { colors } from '@/shared/theme/colors';

type BalanceInput = { unitId: string; status: InstallmentStatus; outstandingAmount: number };

/**
 * What a lot still owes, derived from its own echeances. Mirrors oikos-web's
 * units/utils/unitBalance.ts - see it for why this is a debt (never negative,
 * so it cannot express an avance) rather than a true accounting "solde", and
 * why payments must not be netted against it.
 */
export function unitOutstanding(installments: readonly BalanceInput[], unitId: string): number {
  return outstandingTotal(installments.filter((installment) => installment.unitId === unitId));
}

/**
 * Same palette as the accounting module, with the polarity a copropriétaire
 * expects: here the figure is a debt, so any amount owed is the bad case.
 */
export function getOutstandingColor(outstanding: number): string {
  return outstanding > 0 ? colors.error[500] : colors.success[500];
}
