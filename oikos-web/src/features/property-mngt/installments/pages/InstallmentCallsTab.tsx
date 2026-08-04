import { useState } from 'react';
import { useOutletContext } from 'react-router-dom';
import { useInstallmentCallsByProperty } from '@/features/property-mngt/installments/hooks/useInstallmentCallsByProperty';
import { GenerateInstallmentCallForm } from '@/features/property-mngt/installments/components/GenerateInstallmentCallForm';
import { InstallmentCallRow } from '@/features/property-mngt/installments/components/InstallmentCallRow';
import { Card } from '@/shared/components/Card/Card';
import { Loader } from '@/shared/components/Loader/Loader';
import { Alert } from '@/shared/components/Alert/Alert';
import { EmptyState } from '@/shared/components/EmptyState/EmptyState';
import { Pagination } from '@/shared/components/Pagination/Pagination';
import { getErrorMessage } from '@/shared/utils/getErrorMessage';
import type { GenerateInstallmentCallResult } from '@/features/property-mngt/installments/types/installmentCall.types';
import type { Property } from '@/features/property-mngt/properties/types/property.types';

export function InstallmentCallsTab() {
  const { property } = useOutletContext<{ property: Property }>();
  const [page, setPage] = useState(0);
  const [lastResult, setLastResult] = useState<GenerateInstallmentCallResult | null>(null);

  const installmentCalls = useInstallmentCallsByProperty(property.id, page);

  return (
    <Card className="flex flex-col gap-4">
      <GenerateInstallmentCallForm propertyId={property.id} onGenerated={setLastResult} />

      {lastResult && (
        <Alert
          variant={lastResult.skippedUnitIds.length === 0 ? 'success' : 'error'}
          message={
            lastResult.skippedUnitIds.length === 0
              ? `Appel généré : ${lastResult.chargedUnitIds.length} lot(s) facturé(s).`
              : `Appel généré : ${lastResult.chargedUnitIds.length} lot(s) facturé(s), ${lastResult.skippedUnitIds.length} lot(s) ignoré(s) (${
                  property.duesCalculationMode === 'SHARES' ? 'tantième non configuré' : 'prix non configuré'
                }).`
          }
        />
      )}

      {installmentCalls.isLoading && <Loader label="Chargement des appels à cotisation…" />}
      {installmentCalls.isError && <Alert message={getErrorMessage(installmentCalls.error)} />}
      {installmentCalls.data && installmentCalls.data.content.length === 0 && (
        <EmptyState title="Aucun appel à cotisation pour le moment">
          Générez un appel pour créer les échéances de tous les lots de cette copropriété.
        </EmptyState>
      )}
      {installmentCalls.data && installmentCalls.data.content.length > 0 && (
        <div className="flex flex-col gap-3">
          <ul className="flex flex-col divide-y divide-gray-200 rounded-lg border border-gray-200">
            {installmentCalls.data.content.map((installmentCall) => (
              <InstallmentCallRow key={installmentCall.id} installmentCall={installmentCall} propertyId={property.id} />
            ))}
          </ul>
          <Pagination
            pageNumber={installmentCalls.data.pageNumber}
            totalPages={installmentCalls.data.totalPages}
            onPageChange={setPage}
          />
        </div>
      )}
    </Card>
  );
}
