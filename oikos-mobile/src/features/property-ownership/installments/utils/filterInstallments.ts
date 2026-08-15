import type { OwnedInstallment } from '@/features/property-ownership/installments/types/installment.types';
import { isNotYetDue, isUnsettled } from '@/features/property-ownership/installments/utils/installmentTotals';
import { INSTALLMENT_STATUS_LABELS } from '@/features/property-ownership/installments/constants/installmentStatusLabels';
import { presetStartDate, type DateRangePreset } from '@/shared/utils/datePresets';
import { matchesSearch } from '@/shared/utils/normalizeForSearch';

/** '' means "no filter" for every field. Mirrors oikos-web's filterInstallments.ts. */
export interface MyInstallmentFiltersValue {
  /**
   * 'DUE' groups NOT_SETTLED + PARTIALLY_SETTLED - see isUnsettled. Deliberately
   * on the status alone: the filter lists everything left to pay, including
   * echeances not yet fallen due, which the balances exclude.
   */
  status: 'DUE' | 'SETTLED' | '';
  /**
   * Unsettled echeances dated in the future are hidden by default. They are not
   * owed yet, so they inflate a list read as "what do I have to pay" and, since
   * the totals exclude them, they make the visible rows add up to more than the
   * total. Opt back in through the toggle above the list, never silently.
   */
  includeNotYetDue: boolean;
  unitId: string;
  /** Preset rather than free dates: no date picker on mobile (see datePresets). */
  period: DateRangePreset;
  search: string;
  sortBy: 'DUE_DATE' | 'AMOUNT';
  sortDirection: 'ASC' | 'DESC';
}

export const DEFAULT_MY_INSTALLMENT_FILTERS: MyInstallmentFiltersValue = {
  status: '',
  includeNotYetDue: false,
  unitId: '',
  period: '',
  search: '',
  sortBy: 'DUE_DATE',
  sortDirection: 'DESC',
};

/** Both date spellings are included so "15/01/2026" or "2026-01" both find the row. */
function searchHaystack(installment: OwnedInstallment, unitLabel: string): string {
  return [
    unitLabel,
    INSTALLMENT_STATUS_LABELS[installment.status],
    installment.amount,
    installment.outstandingAmount,
    installment.dueDate,
    new Date(installment.dueDate).toLocaleDateString('fr-FR'),
  ].join(' ');
}

export function filterInstallments(
  installments: readonly OwnedInstallment[],
  filters: MyInstallmentFiltersValue,
  /** unitId -> lot label, so free-text search can match on the lot. */
  unitLabelById: ReadonlyMap<string, string> = new Map(),
  today: Date = new Date(),
): OwnedInstallment[] {
  const from = presetStartDate(filters.period, today);

  const filtered = installments.filter((installment) => {
    if (filters.search && !matchesSearch(searchHaystack(installment, unitLabelById.get(installment.unitId) ?? ''), filters.search)) {
      return false;
    }
    if (filters.status === 'DUE' && !isUnsettled(installment)) {
      return false;
    }
    if (filters.status === 'SETTLED' && isUnsettled(installment)) {
      return false;
    }
    if (!filters.includeNotYetDue && isNotYetDue(installment)) {
      return false;
    }
    if (filters.unitId && installment.unitId !== filters.unitId) {
      return false;
    }
    // Lexicographic on the ISO "YYYY-MM-DD" strings - no timezone-shifting parse.
    if (from && installment.dueDate < from) {
      return false;
    }
    return true;
  });

  const direction = filters.sortDirection === 'ASC' ? 1 : -1;
  return filtered.sort((a, b) => {
    if (filters.sortBy === 'AMOUNT') {
      return (a.amount - b.amount) * direction;
    }
    return a.dueDate.localeCompare(b.dueDate) * direction;
  });
}

/**
 * How many rows the "à échoir" toggle is currently hiding, under the *other*
 * filters in force - so the count always matches what turning it on would add.
 * Surfaced on the toggle itself: hiding rows by default is only acceptable if
 * the list says so.
 */
export function notYetDueHiddenCount(
  installments: readonly OwnedInstallment[],
  filters: MyInstallmentFiltersValue,
  unitLabelById: ReadonlyMap<string, string> = new Map(),
): number {
  if (filters.includeNotYetDue) {
    return 0;
  }
  const withThem = filterInstallments(installments, { ...filters, includeNotYetDue: true }, unitLabelById);
  return withThem.filter((installment) => isNotYetDue(installment)).length;
}
