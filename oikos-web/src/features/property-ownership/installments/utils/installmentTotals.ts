import { isDueBy } from '@/features/property-mngt/installments/utils/installmentDueness';
import { todayIsoDate } from '@/shared/utils/todayIsoDate';
import type { InstallmentStatus } from '@/features/property-mngt/installments/types/installment.types';

// The predicates are shared with the syndic space, which applies the same rule -
// see property-mngt/installments/utils/installmentDueness. Re-exported here so
// the owner-side callers keep one import for "what is owed".
export { hasFallenDue, isDueBy, isNotYetDue, isUnsettled } from '@/features/property-mngt/installments/utils/installmentDueness';

type Owed = { status: InstallmentStatus; dueDate: string; outstandingAmount: number };

/** What is owed at the reference date: settled lines and not-yet-due ones contribute 0. */
export function outstandingTotal(installments: readonly Owed[], asOf: string = todayIsoDate()): number {
  return installments
    .filter((installment) => isDueBy(installment, asOf))
    .reduce((sum, installment) => sum + installment.outstandingAmount, 0);
}
