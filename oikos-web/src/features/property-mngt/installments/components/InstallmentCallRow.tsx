import { useState } from 'react';
import { Button } from '@/shared/components/Button/Button';
import { Loader } from '@/shared/components/Loader/Loader';
import { Alert } from '@/shared/components/Alert/Alert';
import { InstallmentList } from '@/features/property-mngt/installments/components/InstallmentList';
import { useInstallmentCallDetail } from '@/features/property-mngt/installments/hooks/useInstallmentCallDetail';
import { EmptyState } from '@/shared/components/EmptyState/EmptyState';
import { getErrorMessage } from '@/shared/utils/getErrorMessage';
import type { InstallmentCallSummary } from '@/features/property-mngt/installments/types/installmentCall.types';

function formatPeriod(period: string): string {
  const [year, month] = period.split('-');
  const date = new Date(Number(year), Number(month) - 1, 1);
  return date.toLocaleDateString('fr-FR', { month: 'long', year: 'numeric' });
}

interface InstallmentCallRowProps {
  installmentCall: InstallmentCallSummary;
  propertyId: string;
}

export function InstallmentCallRow({ installmentCall, propertyId }: InstallmentCallRowProps) {
  const [isExpanded, setIsExpanded] = useState(false);
  const detail = useInstallmentCallDetail(installmentCall.id, isExpanded);

  return (
    <li className="flex flex-col gap-2 px-3 py-2">
      <div className="flex flex-col gap-1 sm:flex-row sm:items-center sm:justify-between">
        <div>
          <p className="text-sm font-medium capitalize text-gray-900">{formatPeriod(installmentCall.period)}</p>
          <p className="text-sm text-gray-500">
            Échéance le {new Date(installmentCall.dueDate).toLocaleDateString('fr-FR')} · {installmentCall.unitCount} lot(s)
            {' · '}
            {installmentCall.totalAmount} MAD
          </p>
        </div>
        <Button type="button" variant="secondary" onClick={() => setIsExpanded((value) => !value)}>
          {isExpanded ? 'Masquer le détail' : 'Voir le détail'}
        </Button>
      </div>

      {isExpanded && (
        <div className="pt-2">
          {detail.isLoading && <Loader label="Chargement du détail…" />}
          {detail.isError && <Alert message={getErrorMessage(detail.error)} />}
          {detail.data && detail.data.installments.length === 0 && (
            <EmptyState title="Aucune échéance">
              Tous les lots ont été ignorés (prix non configuré).
            </EmptyState>
          )}
          {detail.data && detail.data.installments.length > 0 && (
            <InstallmentList installments={detail.data.installments} propertyId={propertyId} />
          )}
        </div>
      )}
    </li>
  );
}
