import { useNavigate } from 'react-router-dom';
import type { Payment } from '@/features/property-mngt/installments/types/payment.types';
import type { OwnedUnit } from '@/features/property-ownership/units/types/unit.types';
import { formatUnitLabel } from '@/features/property-ownership/units/utils/formatUnitLabel';
import { paymentsTotal } from '@/features/property-ownership/payments/utils/filterPayments';
import { PAYMENT_MODE_LABELS } from '@/features/property-mngt/installments/constants/paymentModeLabels';
import { Badge } from '@/shared/components/Badge/Badge';

interface MyPaymentsTableProps {
  payments: readonly Payment[];
  unitsById: Map<string, OwnedUnit>;
}

export function MyPaymentsTable({ payments, unitsById }: MyPaymentsTableProps) {
  const navigate = useNavigate();

  return (
    <div className="overflow-x-auto">
      <table className="min-w-full divide-y divide-gray-200 dark:divide-gray-800 text-sm">
        <thead>
          <tr className="text-left text-gray-500 dark:text-gray-400">
            <th className="py-2 pr-4 font-medium">Date de valeur</th>
            <th className="py-2 pr-4 font-medium">Lot</th>
            <th className="py-2 pr-4 font-medium">Mode</th>
            <th className="py-2 text-right font-medium">Montant</th>
          </tr>
        </thead>
        <tbody className="divide-y divide-gray-100 dark:divide-gray-800">
          {payments.map((payment) => {
            const unit = unitsById.get(payment.unitId);
            return (
              <tr
                key={payment.id}
                onClick={() => navigate(`/property-ownership/payments/${payment.id}`)}
                className="cursor-pointer hover:bg-gray-50 dark:hover:bg-white/[0.03]"
              >
                <td className="py-2 pr-4 text-gray-700 dark:text-gray-300">
                  {new Date(payment.valueDate).toLocaleDateString('fr-FR')}
                </td>
                <td className="py-2 pr-4 text-gray-500 dark:text-gray-400">{unit ? formatUnitLabel(unit) : '—'}</td>
                <td className="py-2 pr-4">
                  <Badge color="light">{PAYMENT_MODE_LABELS[payment.mode]}</Badge>
                </td>
                <td className="py-2 text-right text-gray-700 dark:text-gray-300">
                  {payment.amount.toLocaleString('fr-FR')} MAD
                </td>
              </tr>
            );
          })}
        </tbody>
        <tfoot>
          <tr className="border-t border-gray-200 dark:border-gray-800 font-medium text-gray-900 dark:text-white/90">
            <td className="py-2 pr-4" colSpan={3}>
              Total versé
            </td>
            <td className="py-2 text-right">{paymentsTotal(payments).toLocaleString('fr-FR')} MAD</td>
          </tr>
        </tfoot>
      </table>
    </div>
  );
}
