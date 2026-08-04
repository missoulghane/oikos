import { useOutletContext } from 'react-router-dom';
import { usePendingLettrages } from '@/features/property-mngt/accounting/hooks/usePendingLettrages';
import { useValidateBulkLettrage } from '@/features/property-mngt/accounting/hooks/useValidateBulkLettrage';
import { PendingLettrageRow } from '@/features/property-mngt/accounting/components/PendingLettrageRow';
import { Button } from '@/shared/components/Button/Button';
import { Card } from '@/shared/components/Card/Card';
import { Loader } from '@/shared/components/Loader/Loader';
import { Alert } from '@/shared/components/Alert/Alert';
import { EmptyState } from '@/shared/components/EmptyState/EmptyState';
import { getErrorMessage } from '@/shared/utils/getErrorMessage';
import type { Property } from '@/features/property-mngt/properties/types/property.types';

export function AccountingLettrageTab() {
  const { property } = useOutletContext<{ property: Property }>();
  const pendingLettrages = usePendingLettrages(property.id);
  const validateBulk = useValidateBulkLettrage(property.id);

  return (
    <Card className="flex flex-col gap-4">
      {pendingLettrages.isLoading && <Loader label="Chargement du lettrage…" />}
      {pendingLettrages.isError && <Alert message={getErrorMessage(pendingLettrages.error)} />}
      {validateBulk.error && <Alert message={getErrorMessage(validateBulk.error)} />}

      {pendingLettrages.data && pendingLettrages.data.length === 0 && (
        <EmptyState title="Rien à lettrer pour le moment">
          Tous les paiements et avoirs disponibles ont déjà été affectés aux échéances correspondantes.
        </EmptyState>
      )}

      {pendingLettrages.data && pendingLettrages.data.length > 0 && (
        <div className="flex flex-col gap-3">
          <Button
            type="button"
            onClick={() => validateBulk.mutate({})}
            isLoading={validateBulk.isPending}
            className="self-start"
          >
            Valider tout
          </Button>
          <ul className="flex flex-col divide-y divide-gray-200 rounded-lg border border-gray-200">
            {pendingLettrages.data.map((pending) => (
              <PendingLettrageRow key={pending.unitId} propertyId={property.id} pending={pending} />
            ))}
          </ul>
        </div>
      )}
    </Card>
  );
}
