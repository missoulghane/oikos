import { Link } from 'react-router-dom';
import { Card } from '@/shared/components/Card/Card';
import { getOutstandingColorClass } from '@/features/property-ownership/units/utils/unitBalance';
import type { OwnedUnit } from '@/features/property-ownership/units/types/unit.types';

interface MyUnitCardProps {
  unit: OwnedUnit;
  /** What this lot still owes; undefined while the echeances are still loading. */
  outstanding?: number;
}

export function MyUnitCard({ unit, outstanding }: MyUnitCardProps) {
  return (
    <Link to={`/property-ownership/units/${unit.propertyId}/${unit.unitId}`} className="block">
      <Card className="flex flex-col gap-1">
        <div className="flex items-start justify-between gap-3">
          <h2 className="font-medium text-gray-900 dark:text-white/90">{unit.propertyName}</h2>
          {outstanding !== undefined && (
            <span className={`shrink-0 text-sm font-semibold ${getOutstandingColorClass(outstanding)}`}>
              {/* Signed like an account statement: what the lot owes shows as a
                  negative balance, so the figure reads the same way as the minus
                  sign a copropriétaire expects on a debit. */}
              {outstanding > 0 ? `Solde : -${outstanding.toLocaleString('fr-FR')} MAD` : 'À jour'}
            </span>
          )}
        </div>
        <p className="text-sm text-gray-500 dark:text-gray-400">
          {unit.buildingName} — Lot {unit.unitNumber}
        </p>
      </Card>
    </Link>
  );
}
