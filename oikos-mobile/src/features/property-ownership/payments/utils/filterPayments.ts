import type { Payment, PaymentMode } from '@/features/property-ownership/payments/types/payment.types';
import { PAYMENT_MODE_LABELS } from '@/features/property-ownership/payments/constants/paymentModeLabels';
import { presetStartDate, type DateRangePreset } from '@/shared/utils/datePresets';
import { matchesSearch } from '@/shared/utils/normalizeForSearch';

/**
 * No status filter: a payment has no lifecycle status in the domain (only
 * installments do). Lot / mode / period are the only dimensions available.
 */
export interface MyPaymentFiltersValue {
  unitId: string;
  mode: PaymentMode | '';
  period: DateRangePreset;
  search: string;
  sortBy: 'VALUE_DATE' | 'AMOUNT';
  sortDirection: 'ASC' | 'DESC';
}

export const DEFAULT_MY_PAYMENT_FILTERS: MyPaymentFiltersValue = {
  unitId: '',
  mode: '',
  period: '',
  search: '',
  sortBy: 'VALUE_DATE',
  sortDirection: 'DESC',
};

/** Both date spellings are included so "15/01/2026" or "2026-01" both find the row. */
function searchHaystack(payment: Payment, unitLabel: string): string {
  return [
    unitLabel,
    PAYMENT_MODE_LABELS[payment.mode],
    payment.amount,
    payment.valueDate,
    new Date(payment.valueDate).toLocaleDateString('fr-FR'),
  ].join(' ');
}

export function filterPayments(
  payments: readonly Payment[],
  filters: MyPaymentFiltersValue,
  /** unitId -> lot label, so free-text search can match on the lot. */
  unitLabelById: ReadonlyMap<string, string> = new Map(),
  today: Date = new Date(),
): Payment[] {
  const from = presetStartDate(filters.period, today);

  const filtered = payments.filter((payment) => {
    if (filters.search && !matchesSearch(searchHaystack(payment, unitLabelById.get(payment.unitId) ?? ''), filters.search)) {
      return false;
    }
    if (filters.unitId && payment.unitId !== filters.unitId) {
      return false;
    }
    if (filters.mode && payment.mode !== filters.mode) {
      return false;
    }
    if (from && payment.valueDate < from) {
      return false;
    }
    return true;
  });

  const direction = filters.sortDirection === 'ASC' ? 1 : -1;
  return filtered.sort((a, b) => {
    if (filters.sortBy === 'AMOUNT') {
      return (a.amount - b.amount) * direction;
    }
    return a.valueDate.localeCompare(b.valueDate) * direction;
  });
}

export function paymentsTotal(payments: readonly Payment[]): number {
  return payments.reduce((sum, payment) => sum + payment.amount, 0);
}
