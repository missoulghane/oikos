import { Link, Navigate, useNavigate, useOutletContext, useParams } from 'react-router-dom';
import { useCurrentUser, canWriteAccounting } from '@/features/identity/me';
import { RecordSupplierPaymentForm } from '@/features/property-mngt/accounting/components/RecordSupplierPaymentForm';
import { Card } from '@/shared/components/Card/Card';
import { Loader } from '@/shared/components/Loader/Loader';
import type { Expense } from '@/features/property-mngt/accounting/types/accounting.types';
import type { Property } from '@/features/property-mngt/properties/types/property.types';

export function CreateSupplierPaymentPage() {
  const { property } = useOutletContext<{ property: Property }>();
  // Set only when reached from a treasury account's operations page (nested
  // under accounting/treasury-accounts/:accountId/expenses/new) - absent for
  // the generic "Nouvelle dépense" entry point on the Dépenses tab.
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
    : `/property-mngt/properties/${property.id}/accounting/expenses`;
  const backLabel = accountId ? "← Retour au compte" : '← Retour aux dépenses';

  function handleSuccess(result: Expense) {
    navigate(
      accountId
        ? `/property-mngt/properties/${property.id}/accounting/treasury-accounts/${accountId}`
        : `/property-mngt/properties/${property.id}/accounting/journal/${result.journalEntryId}`,
    );
  }

  return (
    <div className="flex flex-col gap-6">
      <div>
        <Link to={backTo} className="text-sm text-gray-500 dark:text-gray-400 hover:underline">
          {backLabel}
        </Link>
        <h1 className="text-lg font-semibold text-gray-900 dark:text-white/90">Enregistrer un règlement fournisseur</h1>
      </div>
      <Card>
        <RecordSupplierPaymentForm propertyId={property.id} fixedTreasuryAccountId={accountId} onSuccess={handleSuccess} />
      </Card>
    </div>
  );
}
