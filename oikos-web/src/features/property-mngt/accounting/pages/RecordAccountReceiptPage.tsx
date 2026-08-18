import { Link, Navigate, useNavigate, useOutletContext, useParams } from 'react-router-dom';
import { useCurrentUser, canWriteAccounting } from '@/features/identity/me';
import { RecordOwnerPaymentForm } from '@/features/property-mngt/installments';
import { Card } from '@/shared/components/Card/Card';
import { Loader } from '@/shared/components/Loader/Loader';
import type { Property } from '@/features/property-mngt/properties/types/property.types';

/**
 * Enregistrer une recette. Reached two ways, and the difference is only which
 * fields are already answered: from a treasury account's operations page the
 * account is fixed, and from the syndic dashboard nothing is - the form then
 * asks for the lot and the account itself, which it already knew how to do.
 */
export function RecordAccountReceiptPage() {
  const { property } = useOutletContext<{ property: Property }>();
  // Absent on the property-level entry point (accounting/receipts/new).
  const { accountId } = useParams<{ accountId?: string }>();
  const currentUser = useCurrentUser();
  const navigate = useNavigate();

  if (currentUser.isLoading) {
    return <Loader />;
  }

  if (!currentUser.data || !canWriteAccounting(currentUser.data, property.id)) {
    return <Navigate to="/forbidden" replace />;
  }

  const backTo = accountId
    ? `/property-mngt/properties/${property.id}/accounting/treasury-accounts/${accountId}`
    : `/property-mngt/properties/${property.id}/accounting`;

  return (
    <div className="flex flex-col gap-6">
      <div>
        <Link to={backTo} className="text-sm text-gray-500 dark:text-gray-400 hover:underline">
          {accountId ? '← Retour au compte' : '← Retour à la comptabilité'}
        </Link>
        <h1 className="text-lg font-semibold text-gray-900 dark:text-white/90">Enregistrer une recette</h1>
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
