import { Link, Navigate, useOutletContext } from 'react-router-dom';
import { useCurrentUser, canWriteAccounting } from '@/features/identity/me';
import { RecordBankChargeForm } from '@/features/property-mngt/accounting/components/RecordBankChargeForm';
import { Card } from '@/shared/components/Card/Card';
import { Loader } from '@/shared/components/Loader/Loader';
import type { Property } from '@/features/property-mngt/properties/types/property.types';

export function CreateBankChargePage() {
  const { property } = useOutletContext<{ property: Property }>();
  const currentUser = useCurrentUser();

  if (currentUser.isLoading) {
    return <Loader />;
  }

  if (!currentUser.data || !canWriteAccounting(currentUser.data, property.id)) {
    return <Navigate to="/forbidden" replace />;
  }

  return (
    <div className="flex flex-col gap-6">
      <div>
        <Link
          to={`/property-mngt/properties/${property.id}/accounting`}
          className="text-sm text-gray-500 dark:text-gray-400 hover:underline"
        >
          ← Retour à la comptabilité
        </Link>
        <h1 className="text-lg font-semibold text-gray-900 dark:text-white/90">Enregistrer des frais bancaires</h1>
      </div>
      <Card className="max-w-2xl">
        <RecordBankChargeForm propertyId={property.id} />
      </Card>
    </div>
  );
}
