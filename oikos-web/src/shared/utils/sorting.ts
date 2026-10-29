export type SortDirection = 'ASC' | 'DESC';

/**
 * Clicking the active column flips it; moving to a new column starts at DESC -
 * most recent date / largest amount first, which is what someone reaching for a
 * sort almost always wants at the top.
 */
export function nextSortDirection<TField extends string>(
  clicked: TField,
  activeField: TField,
  direction: SortDirection,
): SortDirection {
  if (clicked !== activeField) {
    return 'DESC';
  }
  return direction === 'DESC' ? 'ASC' : 'DESC';
}

export type SortValue = string | number | null | undefined;

/**
 * Compares two cell values for a client-side sort. Text in French collation with
 * numbers read as numbers (« Lot 2 » before « Lot 10 »); empty values last
 * whichever way the column runs - an empty cell leading a descending sort would
 * bury the rows the reader clicked for.
 */
export function compareSortValues(a: SortValue, b: SortValue, direction: SortDirection): number {
  const aEmpty = a === null || a === undefined || a === '';
  const bEmpty = b === null || b === undefined || b === '';
  if (aEmpty || bEmpty) {
    return aEmpty === bEmpty ? 0 : aEmpty ? 1 : -1;
  }
  const ascending =
    typeof a === 'number' && typeof b === 'number'
      ? a - b
      : String(a).localeCompare(String(b), 'fr', { numeric: true, sensitivity: 'base' });
  return direction === 'ASC' ? ascending : -ascending;
}
