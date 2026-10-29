import type { Payment, PaymentMode } from '@/features/property-mngt/installments/types/payment.types';
import { PAYMENT_MODE_LABELS } from '@/features/property-mngt/installments/constants/paymentModeLabels';
import { matchesSearch } from '@/shared/utils/normalizeForSearch';
import { compareSortValues, type SortValue } from '@/shared/utils/sorting';

/**
 * There is deliberately no status filter: a payment has no lifecycle status in
 * the domain (only installments do), so lot / mode / value-date range are the
 * only dimensions that actually exist on the payload.
 */
export interface MyPaymentFiltersValue {
  unitId: string;
  mode: PaymentMode | '';
  valueDateFrom: string;
  valueDateTo: string;
  search: string;
  sortBy: 'VALUE_DATE' | 'UNIT' | 'MODE' | 'AMOUNT';
  sortDirection: 'ASC' | 'DESC';
}

export const DEFAULT_MY_PAYMENT_FILTERS: MyPaymentFiltersValue = {
  unitId: '',
  mode: '',
  valueDateFrom: '',
  valueDateTo: '',
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

/** Client-side, same rationale as filterInstallments: GET /users/me/payments is a plain array. */
export function filterPayments(
  payments: readonly Payment[],
  filters: MyPaymentFiltersValue,
  /** unitId -> lot label, so free-text search can match on the lot. */
  unitLabelById: ReadonlyMap<string, string> = new Map(),
): Payment[] {
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
    // Lexicographic on the ISO "YYYY-MM-DD" strings - no timezone-shifting parse.
    if (filters.valueDateFrom && payment.valueDate < filters.valueDateFrom) {
      return false;
    }
    if (filters.valueDateTo && payment.valueDate > filters.valueDateTo) {
      return false;
    }
    return true;
  });

  // Value date, most recent first, between equal rows - the list's default order.
  return filtered.sort((a, b) => {
    const primary = compareSortValues(sortKey(a, filters.sortBy, unitLabelById), sortKey(b, filters.sortBy, unitLabelById),
      filters.sortDirection);
    return primary !== 0 ? primary : b.valueDate.localeCompare(a.valueDate);
  });
}

function sortKey(payment: Payment, sortBy: MyPaymentFiltersValue['sortBy'], unitLabelById: ReadonlyMap<string, string>): SortValue {
  switch (sortBy) {
    case 'UNIT':
      return unitLabelById.get(payment.unitId);
    // On the label shown, not the code: the column reads « Chèque », « Espèces »…
    case 'MODE':
      return PAYMENT_MODE_LABELS[payment.mode];
    case 'AMOUNT':
      return payment.amount;
    default:
      return payment.valueDate;
  }
}

export function paymentsTotal(payments: readonly Payment[]): number {
  return payments.reduce((sum, payment) => sum + payment.amount, 0);
}
