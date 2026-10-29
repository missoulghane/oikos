import { describe, expect, it } from 'vitest';
import { compareSortValues } from '@/shared/utils/sorting';

const sorted = (values: (string | number | null)[], direction: 'ASC' | 'DESC') =>
  [...values].sort((a, b) => compareSortValues(a, b, direction));

describe('compareSortValues', () => {
  it('reads the numbers inside a text as numbers, ignoring case and accents', () => {
    expect(sorted(['Lot 10', 'lot 2', 'Lot 1'], 'ASC')).toEqual(['Lot 1', 'lot 2', 'Lot 10']);
    expect(sorted(['Élodie', 'eric', 'Ali'], 'ASC')).toEqual(['Ali', 'Élodie', 'eric']);
  });

  it('keeps empty values last whichever way the column runs', () => {
    expect(sorted([3, null, 1], 'ASC')).toEqual([1, 3, null]);
    expect(sorted([3, null, 1], 'DESC')).toEqual([3, 1, null]);
    expect(sorted(['', 'B', 'A'], 'DESC')).toEqual(['B', 'A', '']);
  });
});
