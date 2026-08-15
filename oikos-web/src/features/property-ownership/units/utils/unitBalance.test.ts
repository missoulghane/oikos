import { describe, expect, it } from 'vitest';
import { getOutstandingColorClass, unitOutstanding } from '@/features/property-ownership/units/utils/unitBalance';

const PAST = '2026-01-15';
const FUTURE = '2026-12-01';
const TODAY = '2026-06-15';

const installments = [
  { unitId: 'unit-1', status: 'NOT_SETTLED' as const, dueDate: PAST, outstandingAmount: 1000 },
  { unitId: 'unit-1', status: 'PARTIALLY_SETTLED' as const, dueDate: PAST, outstandingAmount: 200 },
  { unitId: 'unit-1', status: 'SETTLED' as const, dueDate: PAST, outstandingAmount: 0 },
  { unitId: 'unit-2', status: 'NOT_SETTLED' as const, dueDate: PAST, outstandingAmount: 750 },
  // Not owed yet - excluded from every balance below.
  { unitId: 'unit-1', status: 'NOT_SETTLED' as const, dueDate: FUTURE, outstandingAmount: 5000 },
];

describe('unitOutstanding', () => {
  it('sums only the echeances of the given lot', () => {
    expect(unitOutstanding(installments, 'unit-1', TODAY)).toBe(1200);
    expect(unitOutstanding(installments, 'unit-2', TODAY)).toBe(750);
  });

  it('is 0 for a lot with nothing owed', () => {
    expect(unitOutstanding(installments, 'unit-3', TODAY)).toBe(0);
  });

  it('excludes settled echeances', () => {
    const settledOnly = [{ unitId: 'unit-9', status: 'SETTLED' as const, dueDate: PAST, outstandingAmount: 999 }];

    expect(unitOutstanding(settledOnly, 'unit-9', TODAY)).toBe(0);
  });

  // A yearly appel de fonds must not read as a debt on the day it is raised.
  it('excludes an echeance of the lot that has not fallen due yet', () => {
    // In June the December echeance is not owed; on its due date it joins the balance.
    expect(unitOutstanding(installments, 'unit-1', TODAY)).toBe(1200);
    expect(unitOutstanding(installments, 'unit-1', FUTURE)).toBe(6200);
  });
});

describe('getOutstandingColorClass', () => {
  // The accounting helper reads a balance as cash on hand (more is better). On a
  // debt the polarity flips, so this must never paint an amount owed green.
  it('paints an amount owed red', () => {
    expect(getOutstandingColorClass(5000)).toMatch(/error/);
    expect(getOutstandingColorClass(1)).toMatch(/error/);
  });

  it('paints a lot with nothing owed green', () => {
    expect(getOutstandingColorClass(0)).toMatch(/success/);
  });
});
