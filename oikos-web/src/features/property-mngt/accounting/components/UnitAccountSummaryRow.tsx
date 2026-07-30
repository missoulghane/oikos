import { Link } from 'react-router-dom';
import { useUnit } from '@/features/property-mngt/properties/hooks/useUnit';
import { Loader } from '@/shared/components/Loader/Loader';
import type { UnitAccountSummary } from '@/features/property-mngt/accounting/types/accounting.types';

function formatAmount(value: number): string {
  return `${value.toLocaleString('fr-FR')} MAD`;
}

function balanceColorClass(balance: number): string {
  if (balance < 0) return 'text-error-600';
  if (balance > 0) return 'text-success-600';
  return 'text-gray-500';
}

interface UnitAccountSummaryRowProps {
  propertyId: string;
  summary: UnitAccountSummary;
}

export function UnitAccountSummaryRow({ propertyId, summary }: UnitAccountSummaryRowProps) {
  const unit = useUnit(summary.unitId);

  return (
    <li className="flex flex-col gap-1 px-3 py-2 sm:flex-row sm:items-center sm:justify-between">
      <div>
        {unit.isLoading && <Loader label="Chargement…" />}
        {unit.data && (
          <Link
            to={`/properties/${propertyId}/units/${summary.unitId}`}
            className="text-sm font-medium text-gray-900 hover:underline"
          >
            Lot {unit.data.unitNumber}
          </Link>
        )}
        <p className="text-sm text-gray-500">
          Dû : {formatAmount(summary.totalDue)} · Payé : {formatAmount(summary.totalPaid)}
          {summary.availableAdvance > 0 && ` · Avance : ${formatAmount(summary.availableAdvance)}`}
        </p>
      </div>
      <p className={`text-sm font-medium ${balanceColorClass(summary.currentBalance)}`}>
        {formatAmount(summary.currentBalance)}
      </p>
    </li>
  );
}
