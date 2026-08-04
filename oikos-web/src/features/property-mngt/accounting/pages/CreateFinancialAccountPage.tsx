import { Link, Navigate, useParams } from 'react-router-dom';
import { useCurrentUser, canWriteAccounting } from '@/features/identity/me';
import { CreateFinancialAccountForm } from '@/features/property-mngt/accounting/components/CreateFinancialAccountForm';
import { Card } from '@/shared/components/Card/Card';
import { Loader } from '@/shared/components/Loader/Loader';

export function CreateFinancialAccountPage() {
  const { id } = useParams<{ id: string }>();
  const propertyId = id ?? '';
  const currentUser = useCurrentUser();

  if (currentUser.isLoading) {
    return <Loader />;
  }

  if (!currentUser.data || !canWriteAccounting(currentUser.data, propertyId)) {
    return <Navigate to="/forbidden" replace />;
  }

  return (
    <div className="flex flex-col gap-6">
      <div>
        <Link
          to={`/properties/${propertyId}/accounting/financial-accounts`}
          className="text-sm text-gray-500 hover:underline"
        >
          ← Retour aux comptes financiers
        </Link>
        <h1 className="text-lg font-semibold text-gray-900">Créer un compte financier</h1>
      </div>
      <Card className="max-w-2xl">
        <CreateFinancialAccountForm propertyId={propertyId} />
      </Card>
    </div>
  );
}
