import type { OwnedInstallment } from '@/features/property-ownership/installments/types/installment.types';
import { isDue } from '@/features/property-ownership/installments/utils/installmentTotals';
import { INSTALLMENT_STATUS_LABELS } from '@/features/property-ownership/installments/constants/installmentStatusLabels';
import { presetStartDate, type DateRangePreset } from '@/shared/utils/datePresets';
import { matchesSearch } from '@/shared/utils/normalizeForSearch';

/** '' means "no filter" for every field. Mirrors oikos-web's filterInstallments.ts. */
export interface MyInstallmentFiltersValue {
  /** 'DUE' groups NOT_SETTLED + PARTIALLY_SETTLED - see isDue. */
  status: 'DUE' | 'SETTLED' | '';
  unitId: string;
  /** Preset rather than free dates: no date picker on mobile (see datePresets). */
  period: DateRangePreset;
  search: string;
  sortBy: 'DUE_DATE' | 'AMOUNT';
  sortDirection: 'ASC' | 'DESC';
}

export const DEFAULT_MY_INSTALLMENT_FILTERS: MyInstallmentFiltersValue = {
  status: '',
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
    if (filters.status === 'DUE' && !isDue(installment)) {
      return false;
    }
    if (filters.status === 'SETTLED' && isDue(installment)) {
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
