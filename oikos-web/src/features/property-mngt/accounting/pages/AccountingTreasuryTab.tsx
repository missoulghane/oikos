import { useOutletContext } from 'react-router-dom';
import { isAxiosError } from 'axios';
import { useCurrentUser, canWriteAccounting } from '@/features/identity/me';
import { useTreasurySummary } from '@/features/property-mngt/accounting/hooks/useTreasurySummary';
import { useOpenAccountingExercise } from '@/features/property-mngt/accounting/hooks/useOpenAccountingExercise';
import { TreasurySummaryCards } from '@/features/property-mngt/accounting/components/TreasurySummaryCards';
import { OpenExerciseForm } from '@/features/property-mngt/accounting/components/OpenExerciseForm';
import { Card } from '@/shared/components/Card/Card';
import { Badge } from '@/shared/components/Badge/Badge';
import { Loader } from '@/shared/components/Loader/Loader';
import { Alert } from '@/shared/components/Alert/Alert';
import { EmptyState } from '@/shared/components/EmptyState/EmptyState';
import { getErrorMessage } from '@/shared/utils/getErrorMessage';
import type { Property } from '@/features/property-mngt/properties/types/property.types';

export function AccountingTreasuryTab() {
  const { property } = useOutletContext<{ property: Property }>();
  const currentUser = useCurrentUser();
  const treasurySummary = useTreasurySummary(property.id);
  const openExercise = useOpenAccountingExercise(property.id);

  const canWrite = currentUser.data ? canWriteAccounting(currentUser.data, property.id) : false;
  const noOpenExercise = isAxiosError(openExercise.error) && openExercise.error.response?.status === 400;

  return (
    <div className="flex flex-col gap-6">
      {treasurySummary.isLoading && <Loader label="Chargement de la trésorerie…" />}
      {treasurySummary.isError && <Alert message={getErrorMessage(treasurySummary.error)} />}
      {treasurySummary.data && <TreasurySummaryCards summary={treasurySummary.data} />}

      <div className="flex flex-col gap-4">
        <h2 className="text-base font-semibold text-gray-900">Exercice comptable</h2>

        {openExercise.isLoading && <Loader label="Chargement de l'exercice…" />}
        {openExercise.isError && !noOpenExercise && <Alert message={getErrorMessage(openExercise.error)} />}

        {noOpenExercise && (
          <>
            <EmptyState title="Aucun exercice comptable ouvert">
              {canWrite
                ? "Ouvrez un exercice pour pouvoir enregistrer des dépenses, virements et paiements."
                : "Un gestionnaire doit ouvrir un exercice avant que des opérations puissent être enregistrées."}
            </EmptyState>
            {canWrite && <OpenExerciseForm propertyId={property.id} />}
          </>
        )}

        {openExercise.data && (
          <Card className="flex flex-col gap-1">
            <div className="flex items-center gap-2">
              <p className="text-sm font-medium text-gray-900">{openExercise.data.label}</p>
              <Badge color="success">Ouvert</Badge>
            </div>
            <p className="text-sm text-gray-500">
              Du {new Date(openExercise.data.startDate).toLocaleDateString('fr-FR')} au{' '}
              {new Date(openExercise.data.endDate).toLocaleDateString('fr-FR')}
            </p>
            {openExercise.data.comment && <p className="text-sm text-gray-500">{openExercise.data.comment}</p>}
          </Card>
        )}
      </div>
    </div>
  );
}
