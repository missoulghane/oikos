import { Link, Navigate, useNavigate, useOutletContext, useParams } from 'react-router-dom';
import { useCurrentUser, canWriteAccounting } from '@/features/identity/me';
import { RecordOwnerPaymentForm } from '@/features/property-mngt/installments';
import { Card } from '@/shared/components/Card/Card';
import { Loader } from '@/shared/components/Loader/Loader';
import type { Property } from '@/features/property-mngt/properties/types/property.types';

export function RecordAccountReceiptPage() {
  const { property } = useOutletContext<{ property: Property }>();
  const { accountId } = useParams<{ accountId: string }>();
  const currentUser = useCurrentUser();
  const navigate = useNavigate();

  if (currentUser.isLoading) {
    return <Loader />;
  }

  if (!currentUser.data || !canWriteAccounting(currentUser.data, property.id)) {
    return <Navigate to="/forbidden" replace />;
  }

  const backTo = `/property-mngt/properties/${property.id}/accounting/treasury-accounts/${accountId}`;

  return (
    <div className="flex flex-col gap-6">
      <div>
        <Link to={backTo} className="text-sm text-gray-500 hover:underline">
          ← Retour au compte
        </Link>
        <h1 className="text-lg font-semibold text-gray-900">Enregistrer une recette</h1>
      </div>
      <Card>
        <RecordOwnerPaymentForm
          propertyId={property.id}
          fixedTreasuryAccountId={accountId}
          onSuccess={() => navigate(backTo)}
        />
      </Card>
    </div>
  );
}
