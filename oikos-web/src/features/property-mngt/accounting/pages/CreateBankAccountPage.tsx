import { Link, Navigate, useNavigate, useOutletContext } from 'react-router-dom';
import { useCurrentUser, canWriteAccounting } from '@/features/identity/me';
import { AddBankAccountForm } from '@/features/property-mngt/accounting/components/AddBankAccountForm';
import { Card } from '@/shared/components/Card/Card';
import { Loader } from '@/shared/components/Loader/Loader';
import type { Property } from '@/features/property-mngt/properties/types/property.types';

export function CreateBankAccountPage() {
  const { property } = useOutletContext<{ property: Property }>();
  const currentUser = useCurrentUser();
  const navigate = useNavigate();

  if (currentUser.isLoading) {
    return <Loader />;
  }

  if (!currentUser.data || !canWriteAccounting(currentUser.data, property.id)) {
    return <Navigate to="/forbidden" replace />;
  }

  function handleCreated(accountId: string) {
    navigate(
      accountId
        ? `/property-mngt/properties/${property.id}/accounting/treasury-accounts/${accountId}`
        : `/property-mngt/properties/${property.id}/accounting`,
    );
  }

  return (
    <div className="flex flex-col gap-6">
      <div>
        <Link
          to={`/property-mngt/properties/${property.id}/accounting`}
          className="text-sm text-gray-500 dark:text-gray-400 hover:underline"
        >
          ← Retour à la vue d'ensemble
        </Link>
        <h1 className="text-lg font-semibold text-gray-900 dark:text-white/90">Ajouter un compte bancaire</h1>
      </div>
      <Card>
        <AddBankAccountForm propertyId={property.id} onCreated={handleCreated} />
      </Card>
    </div>
  );
}
