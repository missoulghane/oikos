import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { Button } from '@/shared/components/Button/Button';
import { Alert } from '@/shared/components/Alert/Alert';
import { Badge } from '@/shared/components/Badge/Badge';
import { getErrorMessage } from '@/shared/utils/getErrorMessage';
import { useCancelInstallmentCall } from '@/features/property-mngt/installments/hooks/useCancelInstallmentCall';
import { formatPeriod } from '@/features/property-mngt/installments/utils/formatPeriod';
import type { InstallmentCallSummary } from '@/features/property-mngt/installments/types/installmentCall.types';

interface InstallmentCallRowProps {
  installmentCall: InstallmentCallSummary;
  propertyId: string;
}

/**
 * Un appel comptabilisé ne se supprime pas : l'annuler contre-passe son écriture
 * et le laisse dans la liste, au statut « Annulé ». Ses échéances disparaissent -
 * l'API refuse l'annulation dès que l'une d'elles a reçu un règlement.
 */
export function InstallmentCallRow({ installmentCall, propertyId }: InstallmentCallRowProps) {
  const navigate = useNavigate();
  const [isConfirmingCancel, setIsConfirmingCancel] = useState(false);
  const cancelInstallmentCall = useCancelInstallmentCall(propertyId);
  const isCancelled = installmentCall.status === 'CANCELLED';

  return (
    <li className="flex flex-col gap-2 px-3 py-2">
      <div className="flex flex-col gap-1 sm:flex-row sm:items-center sm:justify-between">
        <div>
          <div className="flex items-center gap-2">
            <p className="text-sm font-medium capitalize text-gray-900 dark:text-white/90">
              {formatPeriod(installmentCall.period)}
            </p>
            {isCancelled && <Badge color="light">Annulé</Badge>}
          </div>
          <p className="text-sm text-gray-500 dark:text-gray-400">
            {isCancelled
              ? `Échéance le ${new Date(installmentCall.dueDate).toLocaleDateString('fr-FR')} · écriture contre-passée`
              : `Échéance le ${new Date(installmentCall.dueDate).toLocaleDateString('fr-FR')} · ${installmentCall.unitCount} lot(s) · ${installmentCall.totalAmount} MAD`}
          </p>
        </div>
        {!isCancelled && !isConfirmingCancel && (
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
            <Button type="button" variant="secondary" onClick={() => setIsConfirmingCancel(true)}>
              Annuler l'appel
            </Button>
          </div>
        )}
      </div>

      {isConfirmingCancel && (
        <div className="flex flex-col gap-2">
          <p className="text-sm text-gray-700 dark:text-gray-300">
            Annuler cet appel de fonds passe une écriture d'extourne qui neutralise son écriture comptable, et retire
            ses {installmentCall.unitCount} échéance(s). L'appel reste visible au statut « Annulé » et la période
            pourra être appelée à nouveau. Impossible si une échéance a déjà reçu un règlement.
          </p>
          {cancelInstallmentCall.isError && <Alert message={getErrorMessage(cancelInstallmentCall.error)} />}
          <div className="flex gap-2">
            <Button
              type="button"
              isLoading={cancelInstallmentCall.isPending}
              onClick={() =>
                cancelInstallmentCall.mutate(installmentCall.id, { onSuccess: () => setIsConfirmingCancel(false) })
              }
            >
              Confirmer l'annulation
            </Button>
            <Button type="button" variant="secondary" onClick={() => setIsConfirmingCancel(false)}>
              Retour
            </Button>
          </div>
        </div>
      )}
    </li>
  );
}
