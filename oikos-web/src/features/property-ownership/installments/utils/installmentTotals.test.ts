import { describe, expect, it } from 'vitest';
import {
  isDueBy,
  isUnsettled,
  outstandingTotal,
} from '@/features/property-ownership/installments/utils/installmentTotals';

const TODAY = '2026-06-15';

const overdue = { status: 'NOT_SETTLED' as const, dueDate: '2026-01-15', outstandingAmount: 1000 };
const partlyPaidOverdue = { status: 'PARTIALLY_SETTLED' as const, dueDate: '2026-03-10', outstandingAmount: 200 };
const settled = { status: 'SETTLED' as const, dueDate: '2026-02-01', outstandingAmount: 0 };
const dueToday = { status: 'NOT_SETTLED' as const, dueDate: TODAY, outstandingAmount: 300 };
const notYetDue = { status: 'NOT_SETTLED' as const, dueDate: '2026-12-01', outstandingAmount: 5000 };

describe('isUnsettled', () => {
  // Status only: the "À régler" filter shows every line left to pay, including
  // those not yet fallen due.
  it('is about the status alone, never the date', () => {
    expect(isUnsettled(overdue)).toBe(true);
    expect(isUnsettled(partlyPaidOverdue)).toBe(true);
    expect(isUnsettled(notYetDue)).toBe(true);
    expect(isUnsettled(settled)).toBe(false);
  });
});

describe('isDueBy', () => {
  it('counts an unpaid echeance whose date has passed', () => {
    expect(isDueBy(overdue, TODAY)).toBe(true);
    expect(isDueBy(partlyPaidOverdue, TODAY)).toBe(true);
  });

  // The whole point of the rule: an appel de fonds raised for the year is not a
  // debt on the day it is issued.
  it('ignores an echeance that has not fallen due yet', () => {
    expect(isDueBy(notYetDue, TODAY)).toBe(false);
  });

  it('counts an echeance falling due on the reference date itself', () => {
    expect(isDueBy(dueToday, TODAY)).toBe(true);
  });

  it('ignores a settled echeance whatever its date', () => {
    expect(isDueBy(settled, TODAY)).toBe(false);
  });
});

describe('outstandingTotal', () => {
  const all = [overdue, partlyPaidOverdue, settled, dueToday, notYetDue];

  it('sums only what has fallen due', () => {
    // 1000 + 200 + 300; the settled one contributes 0 and the December one is not owed yet
    expect(outstandingTotal(all, TODAY)).toBe(1500);
  });

  it('is 0 when nothing has fallen due yet', () => {
    expect(outstandingTotal([notYetDue], TODAY)).toBe(0);
  });

  // Moving the reference date forward brings the December echeance into the
  // balance - the "solde à une date" the need asks for.
  it('grows as the reference date passes each due date', () => {
    expect(outstandingTotal(all, '2026-11-30')).toBe(1500);
    expect(outstandingTotal(all, '2026-12-01')).toBe(6500);
  });

  it('defaults to today when no reference date is given', () => {
    const yesterday = new Date();
    yesterday.setDate(yesterday.getDate() - 1);
    const tomorrow = new Date();
    tomorrow.setDate(tomorrow.getDate() + 1);
    const iso = (date: Date) => date.toLocaleDateString('sv-SE');

    const straddling = [
      { status: 'NOT_SETTLED' as const, dueDate: iso(yesterday), outstandingAmount: 100 },
      { status: 'NOT_SETTLED' as const, dueDate: iso(tomorrow), outstandingAmount: 900 },
    ];

    expect(outstandingTotal(straddling)).toBe(100);
  });
});
