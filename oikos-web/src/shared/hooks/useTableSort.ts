import { useMemo, useState } from 'react';
import { compareSortValues, nextSortDirection, type SortDirection, type SortValue } from '@/shared/utils/sorting';

/**
 * Column sorting for a table whose rows are all on screen already (no paging,
 * no server round trip): the lines of one journal entry, a lot's statement.
 * Same click behaviour as the server-sorted tables (nextSortDirection), so every
 * table of the product answers a header click the same way.
 *
 * Rows equal on the sorted column keep their original relative order -
 * Array.prototype.sort is stable - which is the order the data came in.
 */
export function useTableSort<TRow, TField extends string>(
  rows: readonly TRow[],
  keyOf: (row: TRow, field: TField) => SortValue,
  initial: { field: TField; direction: SortDirection },
) {
  const [sortBy, setSortBy] = useState<TField>(initial.field);
  const [sortDirection, setSortDirection] = useState<SortDirection>(initial.direction);

  const sorted = useMemo(
    () => [...rows].sort((a, b) => compareSortValues(keyOf(a, sortBy), keyOf(b, sortBy), sortDirection)),
    // keyOf is a pure function of its arguments, redefined on every render by
    // its callers: depending on it would re-sort on every render for nothing.
    // eslint-disable-next-line react-hooks/exhaustive-deps
    [rows, sortBy, sortDirection],
  );

  function onSort(field: TField) {
    setSortDirection(nextSortDirection(field, sortBy, sortDirection));
    setSortBy(field);
  }

  return { sorted, sortBy, sortDirection, onSort };
}
