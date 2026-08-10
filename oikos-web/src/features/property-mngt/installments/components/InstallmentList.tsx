import { Badge } from '@/shared/components/Badge/Badge';
import {
  INSTALLMENT_STATUS_BADGE_COLORS,
  INSTALLMENT_STATUS_LABELS,
} from '@/features/property-mngt/installments/constants/installmentStatusLabels';
import { formatPeriod } from '@/features/property-mngt/installments/utils/formatPeriod';
import type { Installment } from '@/features/property-mngt/installments/types/installment.types';

export function InstallmentList({ installments }: { installments: Installment[] }) {
  return (
    <ul className="flex flex-col divide-y divide-gray-200 dark:divide-gray-800 rounded-lg border border-gray-200 dark:border-gray-800">
      {installments.map((installment) => (
        <li
          key={installment.id}
          className="flex flex-col gap-1 px-3 py-2 sm:flex-row sm:items-center sm:justify-between"
        >
          <div>
            <p className="text-sm font-medium text-gray-900 dark:text-white/90">
              Échéance du {new Date(installment.dueDate).toLocaleDateString('fr-FR')} — {installment.amount} MAD
              {installment.status === 'PARTIALLY_SETTLED' && ` (reste ${installment.outstandingAmount} MAD)`}
            </p>
            {installment.period && (
              <p className="text-sm capitalize text-gray-500 dark:text-gray-400">{formatPeriod(installment.period)}</p>
            )}
          </div>
          <Badge color={INSTALLMENT_STATUS_BADGE_COLORS[installment.status]}>
            {INSTALLMENT_STATUS_LABELS[installment.status]}
          </Badge>
        </li>
      ))}
    </ul>
  );
}
