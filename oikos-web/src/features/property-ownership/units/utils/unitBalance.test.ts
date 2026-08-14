import { describe, expect, it } from 'vitest';
import { getOutstandingColorClass, unitOutstanding } from '@/features/property-ownership/units/utils/unitBalance';

const installments = [
  { unitId: 'unit-1', status: 'NOT_SETTLED' as const, outstandingAmount: 1000 },
  { unitId: 'unit-1', status: 'PARTIALLY_SETTLED' as const, outstandingAmount: 200 },
  { unitId: 'unit-1', status: 'SETTLED' as const, outstandingAmount: 0 },
  { unitId: 'unit-2', status: 'NOT_SETTLED' as const, outstandingAmount: 750 },
];

describe('unitOutstanding', () => {
  it('sums only the echeances of the given lot', () => {
    expect(unitOutstanding(installments, 'unit-1')).toBe(1200);
    expect(unitOutstanding(installments, 'unit-2')).toBe(750);
  });

  it('is 0 for a lot with nothing owed', () => {
    expect(unitOutstanding(installments, 'unit-3')).toBe(0);
  });

  it('excludes settled echeances', () => {
    const settledOnly = [{ unitId: 'unit-9', status: 'SETTLED' as const, outstandingAmount: 999 }];

    expect(unitOutstanding(settledOnly, 'unit-9')).toBe(0);
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
