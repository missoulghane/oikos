import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { Button } from '@/shared/components/Button/Button';
import { Alert } from '@/shared/components/Alert/Alert';
import { getErrorMessage } from '@/shared/utils/getErrorMessage';
import { useDeleteInstallmentCall } from '@/features/property-mngt/installments/hooks/useDeleteInstallmentCall';
import { formatPeriod } from '@/features/property-mngt/installments/utils/formatPeriod';
import type { InstallmentCallSummary } from '@/features/property-mngt/installments/types/installmentCall.types';

interface InstallmentCallRowProps {
  installmentCall: InstallmentCallSummary;
  propertyId: string;
}

export function InstallmentCallRow({ installmentCall, propertyId }: InstallmentCallRowProps) {
  const navigate = useNavigate();
  const [isConfirmingDelete, setIsConfirmingDelete] = useState(false);
  const deleteInstallmentCall = useDeleteInstallmentCall(propertyId);

  return (
    <li className="flex flex-col gap-2 px-3 py-2">
      <div className="flex flex-col gap-1 sm:flex-row sm:items-center sm:justify-between">
        <div>
          <p className="text-sm font-medium capitalize text-gray-900 dark:text-white/90">{formatPeriod(installmentCall.period)}</p>
          <p className="text-sm text-gray-500 dark:text-gray-400">
            Échéance le {new Date(installmentCall.dueDate).toLocaleDateString('fr-FR')} · {installmentCall.unitCount} lot(s)
            {' · '}
            {installmentCall.totalAmount} MAD
          </p>
        </div>
        {!isConfirmingDelete && (
          <div className="flex shrink-0 gap-2">
            <Button
              type="button"
              variant="secondary"
              onClick={() =>
                navigate(
                  `/property-mngt/properties/${propertyId}/installments?installmentCallId=${installmentCall.id}`,
                )
              }
            >
              Voir les échéances
            </Button>
            <Button type="button" variant="secondary" onClick={() => setIsConfirmingDelete(true)}>
              Supprimer
            </Button>
          </div>
        )}
      </div>

      {isConfirmingDelete && (
        <div className="flex flex-col gap-2">
          <p className="text-sm text-gray-700 dark:text-gray-300">
            Supprimer cet appel de fonds supprime aussi les {installmentCall.unitCount} échéance(s) associée(s). Cette
            action est irréversible.
          </p>
          {deleteInstallmentCall.isError && <Alert message={getErrorMessage(deleteInstallmentCall.error)} />}
          <div className="flex gap-2">
            <Button
              type="button"
              isLoading={deleteInstallmentCall.isPending}
              onClick={() => deleteInstallmentCall.mutate(installmentCall.id)}
            >
              Confirmer la suppression
            </Button>
            <Button type="button" variant="secondary" onClick={() => setIsConfirmingDelete(false)}>
              Annuler
            </Button>
          </div>
        </div>
      )}
    </li>
  );
}
