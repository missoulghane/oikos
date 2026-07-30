import { Card } from '@/shared/components/Card/Card';
import type { TreasurySummary } from '@/features/property-mngt/accounting/types/accounting.types';

function formatAmount(value: number): string {
  return `${value.toLocaleString('fr-FR')} MAD`;
}

interface TreasurySummaryCardsProps {
  summary: TreasurySummary;
}

export function TreasurySummaryCards({ summary }: TreasurySummaryCardsProps) {
  return (
    <div className="grid grid-cols-1 gap-4 sm:grid-cols-3">
      <Card className="flex flex-col gap-1">
        <p className="text-sm text-gray-500">Caisse</p>
        <p className="text-lg font-semibold text-gray-900">{formatAmount(summary.cashBalance)}</p>
      </Card>
      <Card className="flex flex-col gap-1">
        <p className="text-sm text-gray-500">Banque</p>
        <p className="text-lg font-semibold text-gray-900">{formatAmount(summary.bankBalance)}</p>
      </Card>
      <Card className="flex flex-col gap-1">
        <p className="text-sm text-gray-500">Total</p>
        <p className="text-lg font-semibold text-gray-900">{formatAmount(summary.totalBalance)}</p>
      </Card>
    </div>
  );
}
