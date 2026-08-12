import { Link, useParams } from 'react-router-dom';
import { useUnit } from '@/features/property-mngt/properties/hooks/useUnit';
import { UnitOwnersSection } from '@/features/property-mngt/properties/components/UnitOwnersSection';
import { UnitInstallmentsSection } from '@/features/property-mngt/installments/components/UnitInstallmentsSection';
import { UnitPaymentsSection } from '@/features/property-mngt/installments/components/UnitPaymentsSection';
import { Card } from '@/shared/components/Card/Card';
import { Loader } from '@/shared/components/Loader/Loader';
import { Alert } from '@/shared/components/Alert/Alert';
import { getErrorMessage } from '@/shared/utils/getErrorMessage';

const OWNERSHIP_STATUS_LABELS = {
  AFFECTED: 'Affecté',
  NOT_AFFECTED: 'Non affecté',
} as const;

export function MyUnitDetailPage() {
  const { propertyId, unitId } = useParams<{ propertyId: string; unitId: string }>();
  const id = unitId ?? '';
  const unit = useUnit(id);

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
        <Link to="/property-ownership/units" className="text-sm text-gray-500 dark:text-gray-400 hover:underline">
          ← Retour à mes lots
        </Link>
        <div className="flex flex-col gap-1 sm:flex-row sm:items-center sm:justify-between">
          <h1 className="text-lg font-semibold text-gray-900 dark:text-white/90">
            Lot {unit.data.unitNumber} — {unit.data.unitTypeName}
          </h1>
          <span className="w-fit rounded-full bg-gray-100 dark:bg-white/[0.05] px-2 py-1 text-xs font-medium text-gray-600 dark:text-gray-400">
            {OWNERSHIP_STATUS_LABELS[unit.data.ownershipStatus]}
          </span>
        </div>
        <p className="text-sm text-gray-500 dark:text-gray-400">{unit.data.shares} tantièmes</p>
      </div>

      <Card className="flex flex-col gap-2">
        <h2 className="text-base font-semibold text-gray-900 dark:text-white/90">Propriétaires</h2>
        <UnitOwnersSection unitId={id} propertyId={propertyId ?? ''} canManage={false} />
      </Card>

      <Card className="flex flex-col gap-2">
        <h2 className="text-base font-semibold text-gray-900 dark:text-white/90">Échéances</h2>
        <UnitInstallmentsSection propertyId={propertyId ?? ''} unitId={id} canManage={false} />
      </Card>

      <Card className="flex flex-col gap-2">
        <h2 className="text-base font-semibold text-gray-900 dark:text-white/90">Paiements</h2>
        <UnitPaymentsSection propertyId={propertyId ?? ''} unitId={id} canManage={false} />
      </Card>
    </div>
  );
}
