import type { InstallmentStatus } from '@/features/property-mngt/installments/types/installment.types';
import { todayIsoDate } from '@/shared/utils/todayIsoDate';

type Unsettled = { status: InstallmentStatus };
type Dated = Unsettled & { dueDate: string };

/**
 * Whether an installment is owed, and since when. Lives in property-mngt rather
 * than property-ownership because both spaces apply the very same rule - the
 * syndic list hides the not-yet-due rows exactly like the owner's - and two
 * copies of it would drift.
 *
 * The syndic list applies the rule server-side (see InstallmentFilter's
 * hideNotYetDueAsOf): these helpers cover the client-side lists and the "à
 * échoir" marker.
 */

/**
 * Not fully settled. This is what a "À régler" filter selects on: it answers
 * "is there anything left to pay on this line", whatever its date.
 */
export function isUnsettled(installment: Unsettled): boolean {
  return installment.status !== 'SETTLED';
}

/**
 * Unsettled *and* already fallen due at the reference date - what a balance is
 * made of, and deliberately a different notion from isUnsettled: an installment
 * dated next December is not money owed today.
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
 * set the lists hide by default.
 */
export function isNotYetDue(installment: Dated, asOf: string = todayIsoDate()): boolean {
  return isUnsettled(installment) && !isDueBy(installment, asOf);
}
