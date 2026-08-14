import { describe, expect, it } from 'vitest';
import { isDue, outstandingTotal } from '@/features/property-ownership/installments/utils/installmentTotals';

describe('isDue', () => {
  it('counts a partially settled echeance as still due', () => {
    expect(isDue({ status: 'PARTIALLY_SETTLED' })).toBe(true);
  });

  it('counts an unsettled echeance as due', () => {
    expect(isDue({ status: 'NOT_SETTLED' })).toBe(true);
  });

  it('does not count a settled echeance as due', () => {
    expect(isDue({ status: 'SETTLED' })).toBe(false);
  });
});

describe('outstandingTotal', () => {
  it('sums the outstanding part of everything not settled', () => {
    const total = outstandingTotal([
      { status: 'NOT_SETTLED', outstandingAmount: 1000 },
      { status: 'PARTIALLY_SETTLED', outstandingAmount: 250 },
    ]);

    expect(total).toBe(1250);
  });

  it('excludes settled echeances even when they report a residual amount', () => {
    const total = outstandingTotal([
      { status: 'NOT_SETTLED', outstandingAmount: 1000 },
      { status: 'SETTLED', outstandingAmount: 999 },
    ]);

    expect(total).toBe(1000);
  });

  it('is 0 for an empty list', () => {
    expect(outstandingTotal([])).toBe(0);
  });
});
