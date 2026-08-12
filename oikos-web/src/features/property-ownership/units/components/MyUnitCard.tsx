import { Link } from 'react-router-dom';
import { Card } from '@/shared/components/Card/Card';
import type { OwnedUnit } from '@/features/property-ownership/units/types/unit.types';

export function MyUnitCard({ unit }: { unit: OwnedUnit }) {
  return (
    <Link to={`/property-ownership/units/${unit.propertyId}/${unit.unitId}`} className="block">
      <Card className="flex flex-col gap-1">
        <h2 className="font-medium text-gray-900 dark:text-white/90">{unit.propertyName}</h2>
        <p className="text-sm text-gray-500 dark:text-gray-400">
          {unit.buildingName} — Lot {unit.unitNumber}
        </p>
        <p className="text-sm text-gray-500 dark:text-gray-400">{unit.ownershipShare}% des tantièmes</p>
      </Card>
    </Link>
  );
}
