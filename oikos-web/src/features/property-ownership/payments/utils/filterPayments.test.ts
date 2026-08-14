import { describe, expect, it } from 'vitest';
import {
  DEFAULT_MY_PAYMENT_FILTERS,
  filterPayments,
  paymentsTotal,
} from '@/features/property-ownership/payments/utils/filterPayments';
import type { Payment } from '@/features/property-mngt/installments/types/payment.types';

const payments: Payment[] = [
  { id: 'a', propertyId: 'p1', unitId: 'unit-1', mode: 'BANK_TRANSFER', valueDate: '2026-01-20', amount: 1000, journalEntryId: 'j1' },
  { id: 'b', propertyId: 'p1', unitId: 'unit-2', mode: 'CASH', valueDate: '2026-04-05', amount: 250, journalEntryId: 'j2' },
  { id: 'c', propertyId: 'p1', unitId: 'unit-1', mode: 'CHECK', valueDate: '2026-07-11', amount: 700, journalEntryId: 'j3' },
];

const ids = (result: Payment[]) => result.map((payment) => payment.id);

describe('filterPayments', () => {
  it('returns everything, most recent first, with the default filters', () => {
    expect(ids(filterPayments(payments, DEFAULT_MY_PAYMENT_FILTERS))).toEqual(['c', 'b', 'a']);
  });

  it('filters by lot', () => {
    expect(ids(filterPayments(payments, { ...DEFAULT_MY_PAYMENT_FILTERS, unitId: 'unit-1' }))).toEqual(['c', 'a']);
  });

  it('filters by payment mode', () => {
    expect(ids(filterPayments(payments, { ...DEFAULT_MY_PAYMENT_FILTERS, mode: 'CASH' }))).toEqual(['b']);
  });

  it('filters by value-date range, bounds included', () => {
    const result = filterPayments(payments, {
      ...DEFAULT_MY_PAYMENT_FILTERS,
      valueDateFrom: '2026-01-20',
      valueDateTo: '2026-04-05',
    });

    expect(ids(result)).toEqual(['b', 'a']);
  });

  it('sorts by amount descending when asked', () => {
    const result = filterPayments(payments, {
      ...DEFAULT_MY_PAYMENT_FILTERS,
      sortBy: 'AMOUNT',
      sortDirection: 'DESC',
    });

    expect(ids(result)).toEqual(['a', 'c', 'b']);
  });

  it('does not mutate the input array', () => {
    const original = [...payments];
    filterPayments(payments, { ...DEFAULT_MY_PAYMENT_FILTERS, sortDirection: 'ASC' });

    expect(payments).toEqual(original);
  });
});

describe('paymentsTotal', () => {
  it('sums the amounts of the given payments', () => {
    expect(paymentsTotal(payments)).toBe(1950);
  });

  it('is 0 for an empty list', () => {
    expect(paymentsTotal([])).toBe(0);
  });
});
