import { useNavigate } from 'react-router-dom';
import type { OwnedInstallment } from '@/features/property-ownership/installments/types/ownedInstallment.types';
import type { OwnedUnit } from '@/features/property-ownership/units/types/unit.types';
import { formatUnitLabel } from '@/features/property-ownership/units/utils/formatUnitLabel';
import { isNotYetDue, outstandingTotal } from '@/features/property-ownership/installments/utils/installmentTotals';
import {
  INSTALLMENT_STATUS_BADGE_COLORS,
  INSTALLMENT_STATUS_LABELS,
} from '@/features/property-mngt/installments/constants/installmentStatusLabels';
import { Badge } from '@/shared/components/Badge/Badge';
import { SortableColumnHeader } from '@/shared/components/SortableColumnHeader/SortableColumnHeader';
import type { MyInstallmentFiltersValue } from '@/features/property-ownership/installments/utils/filterInstallments';

interface MyInstallmentsTableProps {
  installments: readonly OwnedInstallment[];
  unitsById: Map<string, OwnedUnit>;
  sortBy: MyInstallmentFiltersValue['sortBy'];
  sortDirection: MyInstallmentFiltersValue['sortDirection'];
  onSort: (field: MyInstallmentFiltersValue['sortBy']) => void;
}

export function MyInstallmentsTable({
  installments,
  unitsById,
  sortBy,
  sortDirection,
  onSort,
}: MyInstallmentsTableProps) {
  const navigate = useNavigate();

  return (
    <div className="overflow-x-auto">
      <table className="min-w-full divide-y divide-gray-200 dark:divide-gray-800 text-sm">
        <thead>
          {/* Lot and Statut are not sortable: the sort runs on the raw payload,
              which carries neither the lot label nor a rank for the status. */}
          <tr className="text-left text-gray-500 dark:text-gray-400">
            <SortableColumnHeader field="DUE_DATE" activeField={sortBy} direction={sortDirection} onSort={onSort}>
              Échéance
            </SortableColumnHeader>
            <th className="py-2 pr-4 font-medium">Lot</th>
            <SortableColumnHeader
              field="AMOUNT"
              activeField={sortBy}
              direction={sortDirection}
              onSort={onSort}
              align="right"
            >
              Montant
            </SortableColumnHeader>
            <th className="py-2 pr-4 text-right font-medium">Reste à payer</th>
            <th className="py-2 font-medium">Statut</th>
          </tr>
        </thead>
        <tbody className="divide-y divide-gray-100 dark:divide-gray-800">
          {installments.map((installment) => {
            const unit = unitsById.get(installment.unitId);
            return (
              <tr
                key={installment.id}
                onClick={() => navigate(`/property-ownership/installments/${installment.id}`)}
                className="cursor-pointer hover:bg-gray-50 dark:hover:bg-white/[0.03]"
              >
                <td className="py-2 pr-4 text-gray-700 dark:text-gray-300">
                  {new Date(installment.dueDate).toLocaleDateString('fr-FR')}
                  {/* Only ever reached with the "Échéances à venir" toggle on,
                      since the list hides these rows by default. Still marked:
                      the footer total ignores them, so without this the visible
                      lines add up to more than the total. */}
                  {isNotYetDue(installment) && (
                    <span className="ml-2 text-xs text-warning-600 dark:text-warning-400">à venir</span>
                  )}
                </td>
                <td className="py-2 pr-4 text-gray-500 dark:text-gray-400">{unit ? formatUnitLabel(unit) : '—'}</td>
                <td className="py-2 pr-4 text-right text-gray-700 dark:text-gray-300">
                  {installment.amount.toLocaleString('fr-FR')} MAD
                </td>
                <td className="py-2 pr-4 text-right text-gray-700 dark:text-gray-300">
                  {installment.outstandingAmount.toLocaleString('fr-FR')} MAD
                </td>
                <td className="py-2">
                  <Badge color={INSTALLMENT_STATUS_BADGE_COLORS[installment.status]}>
                    {INSTALLMENT_STATUS_LABELS[installment.status]}
                  </Badge>
                </td>
              </tr>
            );
          })}
        </tbody>
        <tfoot>
          {/* Total of what the *filtered* rows still owe - so narrowing to one lot
              answers "combien je dois sur ce lot ?" without extra arithmetic. */}
          <tr className="border-t border-gray-200 dark:border-gray-800 font-medium text-gray-900 dark:text-white/90">
            <td className="py-2 pr-4" colSpan={3}>
              Total à régler
              <span className="ml-2 text-xs font-normal text-gray-500 dark:text-gray-400">
                échéances déjà exigibles
              </span>
            </td>
            <td className="py-2 pr-4 text-right">{outstandingTotal(installments).toLocaleString('fr-FR')} MAD</td>
            <td />
          </tr>
        </tfoot>
      </table>
    </div>
  );
}
