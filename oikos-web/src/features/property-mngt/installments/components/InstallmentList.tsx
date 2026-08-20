import { Badge } from '@/shared/components/Badge/Badge';
import {
  INSTALLMENT_STATUS_BADGE_COLORS,
  INSTALLMENT_STATUS_LABELS,
} from '@/features/property-mngt/installments/constants/installmentStatusLabels';
import { formatPeriod } from '@/features/property-mngt/installments/utils/formatPeriod';
import { isNotYetDue } from '@/features/property-mngt/installments/utils/installmentDueness';
import { SortableColumnHeader } from '@/shared/components/SortableColumnHeader/SortableColumnHeader';
import type {
  Installment,
  InstallmentSortField,
  SortDirection,
} from '@/features/property-mngt/installments/types/installment.types';

interface InstallmentListProps {
  installments: Installment[];
  sortBy: InstallmentSortField;
  sortDirection: SortDirection;
  onSort: (field: InstallmentSortField) => void;
}

/**
 * A table rather than the list of rows it used to be: sorting is driven by
 * clicking a column header, as everywhere else, and a header needs a column to
 * sit on. Only DUE_DATE and AMOUNT are sortable - they are the two fields the
 * API can order on (INSTALLMENT_SORT_FIELDS), and status is computed rather
 * than stored, so there is nothing to sort it by.
 */
export function InstallmentList({ installments, sortBy, sortDirection, onSort }: InstallmentListProps) {
  return (
    <div className="overflow-x-auto rounded-lg border border-gray-200 dark:border-gray-800">
      <table className="min-w-full divide-y divide-gray-200 dark:divide-gray-800 text-sm">
        <thead>
          <tr className="text-left text-gray-500 dark:text-gray-400">
            <SortableColumnHeader
              field="DUE_DATE"
              activeField={sortBy}
              direction={sortDirection}
              onSort={onSort}
              className="px-3"
            >
              Échéance
            </SortableColumnHeader>
            <SortableColumnHeader
              field="AMOUNT"
              activeField={sortBy}
              direction={sortDirection}
              onSort={onSort}
              align="right"
              className="px-3"
            >
              Montant
            </SortableColumnHeader>
            <th className="px-3 py-2 font-medium">Lot</th>
            <th className="px-3 py-2 text-right font-medium">Reste à payer</th>
            <th className="px-3 py-2 font-medium">Appel de fonds</th>
            <th className="px-3 py-2 font-medium">Statut</th>
          </tr>
        </thead>
        <tbody className="divide-y divide-gray-100 dark:divide-gray-800">
          {installments.map((installment) => (
            <tr key={installment.id}>
              <td className="px-3 py-2 text-gray-900 dark:text-white/90">
                {new Date(installment.dueDate).toLocaleDateString('fr-FR')}
                {/* Only ever reached with the "à venir" filter on, since these
                    rows are left out by default - marked so a line that is not
                    owed yet is not read as arrears. */}
                {isNotYetDue(installment) && (
                  <span className="ml-2 text-xs text-warning-600 dark:text-warning-400">à venir</span>
                )}
              </td>
              <td className="px-3 py-2 text-right text-gray-700 dark:text-gray-300">{installment.amount} MAD</td>
              {/* Not sortable: the lot is resolved alongside the page, not a column the API orders on. */}
              <td className="px-3 py-2 text-gray-500 dark:text-gray-400">{installment.unitNumber ?? '—'}</td>
              <td className="px-3 py-2 text-right text-gray-700 dark:text-gray-300">
                {installment.outstandingAmount} MAD
              </td>
              <td className="px-3 py-2 capitalize text-gray-500 dark:text-gray-400">
                {installment.period ? formatPeriod(installment.period) : '—'}
              </td>
              <td className="px-3 py-2">
                <Badge color={INSTALLMENT_STATUS_BADGE_COLORS[installment.status]}>
                  {INSTALLMENT_STATUS_LABELS[installment.status]}
                </Badge>
              </td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}
