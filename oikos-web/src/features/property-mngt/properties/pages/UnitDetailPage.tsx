import { Link, useParams } from 'react-router-dom';
import { useUnit } from '@/features/property-mngt/properties/hooks/useUnit';
import { useBuilding } from '@/features/property-mngt/properties/hooks/useBuilding';
import { UnitOwnersSection } from '@/features/property-mngt/properties/components/UnitOwnersSection';
import { UnitInstallmentsSection } from '@/features/property-mngt/installments/components/UnitInstallmentsSection';
import { Card } from '@/shared/components/Card/Card';
import { Loader } from '@/shared/components/Loader/Loader';
import { Alert } from '@/shared/components/Alert/Alert';
import { getErrorMessage } from '@/shared/utils/getErrorMessage';

const OWNERSHIP_STATUS_LABELS = {
  AFFECTED: 'Affecté',
  NOT_AFFECTED: 'Non affecté',
} as const;

export function UnitDetailPage() {
  const { id } = useParams<{ id: string }>();
  const unitId = id ?? '';
  const unit = useUnit(unitId);
  const building = useBuilding(unit.data?.buildingId);

  if (unit.isLoading) {
    return <Loader label="Chargement du lot…" />;
  }

  if (unit.isError) {
    return <Alert message={getErrorMessage(unit.error)} />;
  }

  if (!unit.data) {
    return null;
  }

  return (
    <div className="flex flex-col gap-6">
      <div>
        <Link to="/properties" className="text-sm text-slate-500 hover:underline">
          ← Retour aux copropriétés
        </Link>
        <div className="flex flex-col gap-1 sm:flex-row sm:items-center sm:justify-between">
          <h1 className="text-lg font-semibold text-slate-900">
            Lot {unit.data.unitNumber} — {unit.data.unitTypeName}
          </h1>
          <span className="w-fit rounded-full bg-slate-100 px-2 py-1 text-xs font-medium text-slate-600">
            {OWNERSHIP_STATUS_LABELS[unit.data.ownershipStatus]}
          </span>
        </div>
        <p className="text-sm text-slate-500">{unit.data.shares} tantièmes</p>
      </div>

      <Card className="flex flex-col gap-2">
        <h2 className="text-base font-semibold text-slate-900">Propriétaires</h2>
        <UnitOwnersSection unitId={unitId} propertyId={building.data?.propertyId} />
      </Card>

      <Card className="flex flex-col gap-2">
        <h2 className="text-base font-semibold text-slate-900">Échéances</h2>
        <UnitInstallmentsSection unitId={unitId} />
      </Card>
    </div>
  );
}
