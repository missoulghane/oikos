import { Link } from 'react-router-dom';
import {
  INSTALLMENT_STATUS_CLASSES,
  INSTALLMENT_STATUS_LABELS,
} from '@/features/property-mngt/installments/constants/installmentStatusLabels';
import type { Installment } from '@/features/property-mngt/installments/types/installment.types';

export function InstallmentList({ installments }: { installments: Installment[] }) {
  return (
    <ul className="flex flex-col divide-y divide-slate-200 rounded-md border border-slate-200">
      {installments.map((installment) => (
        <li
          key={installment.id}
          className="flex flex-col gap-1 px-3 py-2 sm:flex-row sm:items-center sm:justify-between"
        >
          <div>
            <p className="text-sm font-medium text-slate-900">
              Échéance du {new Date(installment.dueDate).toLocaleDateString('fr-FR')} — {installment.amount} MAD
            </p>
            <p className="text-sm text-slate-500">
              <Link to={`/units/${installment.unitId}`} className="hover:underline">
                Lot {installment.unitId}
              </Link>
            </p>
          </div>
          <span
            className={`w-fit rounded-full px-2 py-1 text-xs font-medium ${INSTALLMENT_STATUS_CLASSES[installment.status]}`}
          >
            {INSTALLMENT_STATUS_LABELS[installment.status]}
          </span>
        </li>
      ))}
    </ul>
  );
}
