import { Loader } from '@/shared/components/Loader/Loader';
import { Alert } from '@/shared/components/Alert/Alert';
import { useUnitInstallments } from '@/features/property-mngt/installments/hooks/useUnitInstallments';
import {
  INSTALLMENT_STATUS_CLASSES,
  INSTALLMENT_STATUS_LABELS,
} from '@/features/property-mngt/installments/constants/installmentStatusLabels';
import { getErrorMessage } from '@/shared/utils/getErrorMessage';

export function UnitInstallmentsSection({ unitId }: { unitId: string }) {
  const { data, isLoading, isError, error } = useUnitInstallments(unitId);

  if (isLoading) {
    return <Loader label="Chargement des échéances…" />;
  }

  if (isError) {
    return <Alert message={getErrorMessage(error)} />;
  }

  if (!data || data.length === 0) {
    return <p className="text-sm text-slate-400">Aucune échéance pour le moment.</p>;
  }

  return (
    <ul className="flex flex-col divide-y divide-slate-100">
      {data.map((installment) => (
        <li key={installment.id} className="flex items-center justify-between py-2 text-sm">
          <div>
            <p className="text-slate-700">
              Échéance du {new Date(installment.dueDate).toLocaleDateString('fr-FR')} — {installment.amount} MAD
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
