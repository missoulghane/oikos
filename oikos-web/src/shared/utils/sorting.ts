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
