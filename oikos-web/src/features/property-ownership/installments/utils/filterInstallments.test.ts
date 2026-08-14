import { describe, expect, it } from 'vitest';
import {
  DEFAULT_MY_INSTALLMENT_FILTERS,
  filterInstallments,
} from '@/features/property-ownership/installments/utils/filterInstallments';
import type { OwnedInstallment } from '@/features/property-ownership/installments/types/ownedInstallment.types';

const installments: OwnedInstallment[] = [
  { id: 'a', unitId: 'unit-1', dueDate: '2026-01-15', amount: 1000, outstandingAmount: 1000, status: 'NOT_SETTLED' },
  { id: 'b', unitId: 'unit-2', dueDate: '2026-03-10', amount: 500, outstandingAmount: 200, status: 'PARTIALLY_SETTLED' },
  { id: 'c', unitId: 'unit-1', dueDate: '2026-06-01', amount: 800, outstandingAmount: 0, status: 'SETTLED' },
];

const ids = (result: OwnedInstallment[]) => result.map((installment) => installment.id);

describe('filterInstallments', () => {
  it('returns everything, most recent first, with the default filters', () => {
    expect(ids(filterInstallments(installments, DEFAULT_MY_INSTALLMENT_FILTERS))).toEqual(['c', 'b', 'a']);
  });

  it('groups unsettled and partially settled under the "À régler" status', () => {
    const result = filterInstallments(installments, { ...DEFAULT_MY_INSTALLMENT_FILTERS, status: 'DUE' });

    expect(ids(result).sort()).toEqual(['a', 'b']);
  });

  it('keeps only settled echeances under the "Payée" status', () => {
    const result = filterInstallments(installments, { ...DEFAULT_MY_INSTALLMENT_FILTERS, status: 'SETTLED' });

    expect(ids(result)).toEqual(['c']);
  });

  it('filters by lot', () => {
    const result = filterInstallments(installments, { ...DEFAULT_MY_INSTALLMENT_FILTERS, unitId: 'unit-1' });

    expect(ids(result)).toEqual(['c', 'a']);
  });

  it('filters by due-date range, bounds included', () => {
    const result = filterInstallments(installments, {
      ...DEFAULT_MY_INSTALLMENT_FILTERS,
      dueDateFrom: '2026-01-15',
      dueDateTo: '2026-03-10',
    });

    expect(ids(result)).toEqual(['b', 'a']);
  });

  it('combines lot and status', () => {
    const result = filterInstallments(installments, {
      ...DEFAULT_MY_INSTALLMENT_FILTERS,
      unitId: 'unit-1',
      status: 'DUE',
    });

    expect(ids(result)).toEqual(['a']);
  });

  it('sorts by amount ascending when asked', () => {
    const result = filterInstallments(installments, {
      ...DEFAULT_MY_INSTALLMENT_FILTERS,
      sortBy: 'AMOUNT',
      sortDirection: 'ASC',
    });

    expect(ids(result)).toEqual(['b', 'c', 'a']);
  });

  it('matches the lot label through the search term', () => {
    const result = filterInstallments(
      installments,
      { ...DEFAULT_MY_INSTALLMENT_FILTERS, search: 'Résidence' },
      new Map([['unit-2', 'Résidence Al Amal — Bât B — Lot B2']]),
    );

    expect(ids(result)).toEqual(['b']);
  });

  it('ignores accents in both the query and the label', () => {
    const labels = new Map([['unit-2', 'Résidence Al Amal — Bât B — Lot B2']]);

    expect(ids(filterInstallments(installments, { ...DEFAULT_MY_INSTALLMENT_FILTERS, search: 'residence' }, labels))).toEqual(['b']);
    expect(ids(filterInstallments(installments, { ...DEFAULT_MY_INSTALLMENT_FILTERS, search: 'bat' }, labels))).toEqual(['b']);
  });

  it('requires every term of a multi-word search to match', () => {
    const labels = new Map([
      ['unit-1', 'Résidence Al Amal — Bât A — Lot A1'],
      ['unit-2', 'Résidence Al Amal — Bât B — Lot B2'],
    ]);

    expect(ids(filterInstallments(installments, { ...DEFAULT_MY_INSTALLMENT_FILTERS, search: 'amal b2' }, labels))).toEqual(['b']);
  });

  it('searches the amount and the status label too', () => {
    expect(ids(filterInstallments(installments, { ...DEFAULT_MY_INSTALLMENT_FILTERS, search: '1000' }))).toEqual(['a']);
    expect(ids(filterInstallments(installments, { ...DEFAULT_MY_INSTALLMENT_FILTERS, search: 'non soldee' }))).toEqual(['a']);
  });

  it('finds a row by its due date, written either way', () => {
    expect(ids(filterInstallments(installments, { ...DEFAULT_MY_INSTALLMENT_FILTERS, search: '2026-03-10' }))).toEqual(['b']);
    expect(ids(filterInstallments(installments, { ...DEFAULT_MY_INSTALLMENT_FILTERS, search: '10/03/2026' }))).toEqual(['b']);
  });

  it('does not mutate the input array', () => {
    const original = [...installments];
    filterInstallments(installments, { ...DEFAULT_MY_INSTALLMENT_FILTERS, sortDirection: 'ASC' });

    expect(installments).toEqual(original);
  });
});
