import { useOutletContext } from 'react-router-dom';
import { useUnitAccountSummaries } from '@/features/property-mngt/accounting/hooks/useUnitAccountSummaries';
import { UnitAccountSummaryRow } from '@/features/property-mngt/accounting/components/UnitAccountSummaryRow';
import { Card } from '@/shared/components/Card/Card';
import { Loader } from '@/shared/components/Loader/Loader';
import { Alert } from '@/shared/components/Alert/Alert';
import { EmptyState } from '@/shared/components/EmptyState/EmptyState';
import { getErrorMessage } from '@/shared/utils/getErrorMessage';
import type { Property } from '@/features/property-mngt/properties/types/property.types';

export function AccountingUnitsTab() {
  const { property } = useOutletContext<{ property: Property }>();
  const unitAccountSummaries = useUnitAccountSummaries(property.id);

  return (
    <Card className="flex flex-col gap-4">
      {unitAccountSummaries.isLoading && <Loader label="Chargement des comptes des lots…" />}
      {unitAccountSummaries.isError && <Alert message={getErrorMessage(unitAccountSummaries.error)} />}
      {unitAccountSummaries.data && unitAccountSummaries.data.length === 0 && (
        <EmptyState title="Aucun lot pour le moment" />
      )}
      {unitAccountSummaries.data && unitAccountSummaries.data.length > 0 && (
        <ul className="flex flex-col divide-y divide-gray-200 rounded-lg border border-gray-200">
          {unitAccountSummaries.data.map((summary) => (
            <UnitAccountSummaryRow key={summary.unitId} propertyId={property.id} summary={summary} />
          ))}
        </ul>
      )}
    </Card>
  );
}
