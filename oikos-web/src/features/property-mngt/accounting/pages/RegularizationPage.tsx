import { Link, Navigate, useParams } from 'react-router-dom';
import { useCurrentUser, canWriteAccounting } from '@/features/identity/me';
import { RegularizationForm } from '@/features/property-mngt/accounting/components/RegularizationForm';
import { Card } from '@/shared/components/Card/Card';
import { Loader } from '@/shared/components/Loader/Loader';

export function RegularizationPage() {
  const { propertyId, unitId } = useParams<{ propertyId: string; unitId: string }>();
  const currentPropertyId = propertyId ?? '';
  const currentUnitId = unitId ?? '';
  const currentUser = useCurrentUser();

  if (currentUser.isLoading) {
    return <Loader />;
  }

  if (!currentUser.data || !canWriteAccounting(currentUser.data, currentPropertyId)) {
    return <Navigate to="/forbidden" replace />;
  }

  return (
    <div className="flex flex-col gap-6">
      <div>
        <Link
          to={`/property-mngt/properties/${currentPropertyId}/units/${currentUnitId}`}
          className="text-sm text-gray-500 hover:underline"
        >
          ← Retour au lot
        </Link>
        <h1 className="text-lg font-semibold text-gray-900">Enregistrer une régularisation</h1>
      </div>
      <Card className="max-w-2xl">
        <RegularizationForm propertyId={currentPropertyId} unitId={currentUnitId} />
      </Card>
    </div>
  );
}
