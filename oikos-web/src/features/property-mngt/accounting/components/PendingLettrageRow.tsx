import { Link } from 'react-router-dom';
import { useUnit } from '@/features/property-mngt/properties/hooks/useUnit';
import { useValidateUnitLettrage } from '@/features/property-mngt/accounting/hooks/useValidateUnitLettrage';
import { Button } from '@/shared/components/Button/Button';
import { Loader } from '@/shared/components/Loader/Loader';
import { Alert } from '@/shared/components/Alert/Alert';
import { getErrorMessage } from '@/shared/utils/getErrorMessage';
import type { PendingLettrage } from '@/features/property-mngt/accounting/types/accounting.types';

function formatAmount(value: number): string {
  return `${value.toLocaleString('fr-FR')} MAD`;
}

interface PendingLettrageRowProps {
  propertyId: string;
  pending: PendingLettrage;
}

export function PendingLettrageRow({ propertyId, pending }: PendingLettrageRowProps) {
  const unit = useUnit(pending.unitId);
  const { mutate, isPending, error } = useValidateUnitLettrage(propertyId, pending.unitId);

  return (
    <li className="flex flex-col gap-1 px-3 py-2">
      <div className="flex flex-col gap-1 sm:flex-row sm:items-center sm:justify-between">
        <div>
          {unit.isLoading && <Loader label="Chargement…" />}
          {unit.data && (
            <Link
              to={`/properties/${propertyId}/units/${pending.unitId}`}
              className="text-sm font-medium text-gray-900 hover:underline"
            >
              Lot {unit.data.unitNumber}
            </Link>
          )}
          <p className="text-sm text-gray-500">
            {formatAmount(pending.proposedAmount)} vont être affectés
            {pending.remainingUnmatchedDebitAfter > 0 &&
              ` · restera dû : ${formatAmount(pending.remainingUnmatchedDebitAfter)}`}
            {pending.remainingUnallocatedCreditAfter > 0 &&
              ` · avance restante : ${formatAmount(pending.remainingUnallocatedCreditAfter)}`}
          </p>
        </div>
        <Button type="button" variant="secondary" onClick={() => mutate()} isLoading={isPending}>
          Valider
        </Button>
      </div>
      {error && <Alert message={getErrorMessage(error)} />}
    </li>
  );
}
