import { Link, useOutletContext } from 'react-router-dom';
import { isAxiosError } from 'axios';
import { useCurrentUser, canWriteAccounting } from '@/features/identity/me';
import { useOpenExercise } from '@/features/property-mngt/accounting/hooks/useOpenExercise';
import { OpenExerciseForm } from '@/features/property-mngt/accounting/components/OpenExerciseForm';
import { ClosePeriodForm } from '@/features/property-mngt/accounting/components/ClosePeriodForm';
import { Card } from '@/shared/components/Card/Card';
import { Loader } from '@/shared/components/Loader/Loader';
import { Alert } from '@/shared/components/Alert/Alert';
import { Badge } from '@/shared/components/Badge/Badge';
import { getErrorMessage } from '@/shared/utils/getErrorMessage';
import { EXERCISE_STATUS_BADGE_COLORS, EXERCISE_STATUS_LABELS } from '@/features/property-mngt/accounting/constants/accountingLabels';
import type { Property } from '@/features/property-mngt/properties/types/property.types';

export function AccountingOverviewTab() {
  const { property } = useOutletContext<{ property: Property }>();
  const currentUser = useCurrentUser();
  const openExercise = useOpenExercise(property.id);
  const canWrite = currentUser.data ? canWriteAccounting(currentUser.data, property.id) : false;

  const hasNoOpenExercise = isAxiosError(openExercise.error) && openExercise.error.response?.status === 400;

  return (
    <div className="flex flex-col gap-6">
      <Card className="flex flex-col gap-4">
        <h2 className="text-base font-semibold text-gray-900">Exercice comptable</h2>

        {openExercise.isLoading && <Loader label="Chargement de l'exercice…" />}

        {openExercise.isError && !hasNoOpenExercise && <Alert message={getErrorMessage(openExercise.error)} />}

        {openExercise.isError && hasNoOpenExercise && (
          <>
            <p className="text-sm text-gray-500">
              Aucun exercice comptable ouvert pour cette copropriété. Toute écriture (appel de fonds, règlement,
              facture…) nécessite un exercice ouvert.
            </p>
            {canWrite && <OpenExerciseForm propertyId={property.id} />}
          </>
        )}

        {openExercise.data && (
          <>
            <div className="flex items-center gap-3">
              <p className="text-sm font-medium text-gray-900">{openExercise.data.label}</p>
              <Badge color={EXERCISE_STATUS_BADGE_COLORS[openExercise.data.status]}>
                {EXERCISE_STATUS_LABELS[openExercise.data.status]}
              </Badge>
            </div>
            <p className="text-sm text-gray-500">
              Du {new Date(openExercise.data.startDate).toLocaleDateString('fr-FR')} au{' '}
              {new Date(openExercise.data.endDate).toLocaleDateString('fr-FR')}
              {openExercise.data.comment && ` · ${openExercise.data.comment}`}
            </p>

            {canWrite && (
              <div className="border-t border-gray-200 pt-4">
                <ClosePeriodForm propertyId={property.id} />
              </div>
            )}
          </>
        )}
      </Card>

      {canWrite && (
        <Card className="flex flex-col gap-3">
          <h2 className="text-base font-semibold text-gray-900">Opérations</h2>
          <div className="flex flex-wrap gap-3">
            <Link
              to={`/property-mngt/properties/${property.id}/accounting/expenses/new`}
              className="inline-flex min-h-11 items-center rounded-lg bg-brand-500 px-4 py-2 text-sm font-medium text-white shadow-theme-xs hover:bg-brand-600"
            >
              Facture fournisseur
            </Link>
            <Link
              to={`/property-mngt/properties/${property.id}/accounting/supplier-payments/new`}
              className="inline-flex min-h-11 items-center rounded-lg bg-white px-4 py-2 text-sm font-medium text-gray-700 shadow-theme-xs ring-1 ring-inset ring-gray-300 hover:bg-gray-50"
            >
              Règlement fournisseur
            </Link>
            <Link
              to={`/property-mngt/properties/${property.id}/accounting/payroll-expenses/new`}
              className="inline-flex min-h-11 items-center rounded-lg bg-white px-4 py-2 text-sm font-medium text-gray-700 shadow-theme-xs ring-1 ring-inset ring-gray-300 hover:bg-gray-50"
            >
              Charge de personnel
            </Link>
            <Link
              to={`/property-mngt/properties/${property.id}/accounting/bank-charges/new`}
              className="inline-flex min-h-11 items-center rounded-lg bg-white px-4 py-2 text-sm font-medium text-gray-700 shadow-theme-xs ring-1 ring-inset ring-gray-300 hover:bg-gray-50"
            >
              Frais bancaires
            </Link>
          </div>
        </Card>
      )}
    </div>
  );
}
