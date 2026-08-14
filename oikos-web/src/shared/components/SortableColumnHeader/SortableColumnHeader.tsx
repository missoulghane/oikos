import type { ReactNode } from 'react';
import type { SortDirection } from '@/shared/utils/sorting';

interface SortableColumnHeaderProps<TField extends string> {
  field: TField;
  activeField: TField;
  direction: SortDirection;
  /** Called with the clicked column; the parent decides the resulting direction (see nextSortDirection). */
  onSort: (field: TField) => void;
  align?: 'left' | 'right';
  /** Padding utilities for the cell, so the header lines up with its own column's body cells. */
  className?: string;
  children: ReactNode;
}

/**
 * A `<th>` whose label is a button toggling the table's sort, replacing the
 * "Trier par"/"Ordre" selects that used to sit in the filter bar.
 *
 * `aria-sort` is what carries the state for assistive tech - the arrow alone is
 * invisible to a screen reader, and it is drawn only on the active column so a
 * table never looks sorted on three columns at once.
 */
export function SortableColumnHeader<TField extends string>({
  field,
  activeField,
  direction,
  onSort,
  align = 'left',
  className = 'pr-4',
  children,
}: SortableColumnHeaderProps<TField>) {
  const isActive = field === activeField;

  return (
    <th
      aria-sort={isActive ? (direction === 'ASC' ? 'ascending' : 'descending') : 'none'}
      className={`py-2 font-medium ${align === 'right' ? 'text-right' : 'text-left'} ${className}`}
    >
      <button
        type="button"
        onClick={() => onSort(field)}
        className={`flex min-h-11 items-center gap-1 hover:text-gray-700 dark:hover:text-gray-300 ${
          align === 'right' ? 'ml-auto flex-row-reverse' : ''
        } ${isActive ? 'text-gray-700 dark:text-gray-300' : ''}`}
      >
        {children}
        <svg
          width="14"
          height="14"
          viewBox="0 0 24 24"
          fill="none"
          xmlns="http://www.w3.org/2000/svg"
          aria-hidden="true"
          className={`shrink-0 transition-opacity ${isActive ? 'opacity-100' : 'opacity-0 group-hover:opacity-40'}`}
        >
          <path
            d={direction === 'ASC' ? 'm6 15 6-6 6 6' : 'm6 9 6 6 6-6'}
            stroke="currentColor"
            strokeWidth="1.5"
            strokeLinecap="round"
            strokeLinejoin="round"
          />
        </svg>
      </button>
    </th>
  );
}
