import { describe, expect, it } from 'vitest';
import { countActiveFilters } from '@/shared/utils/countActiveFilters';

const defaults = { status: '', unitId: '', sortBy: 'DUE_DATE', sortDirection: 'DESC' };

describe('countActiveFilters', () => {
  it('is 0 when nothing differs from the defaults', () => {
    expect(countActiveFilters({ ...defaults }, defaults)).toBe(0);
  });

  it('counts each field that differs', () => {
    expect(countActiveFilters({ ...defaults, status: 'DUE', unitId: 'unit-1' }, defaults)).toBe(2);
  });

  it('ignores the keys it is told to ignore', () => {
    const value = { ...defaults, sortDirection: 'ASC', status: 'DUE' };

    expect(countActiveFilters(value, defaults, ['sortBy', 'sortDirection'])).toBe(1);
  });

  it('does not count a sort change on its own once sort keys are ignored', () => {
    const value = { ...defaults, sortBy: 'AMOUNT' };

    expect(countActiveFilters(value, defaults, ['sortBy', 'sortDirection'])).toBe(0);
  });
});
