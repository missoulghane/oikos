import { Link, Navigate, useParams } from 'react-router-dom';
import { useCurrentUser, canWriteAccounting } from '@/features/identity/me';
import { useFinancialAccounts } from '@/features/property-mngt/accounting/hooks/useFinancialAccounts';
import { RecordExpenseForm } from '@/features/property-mngt/accounting/components/RecordExpenseForm';
import { Card } from '@/shared/components/Card/Card';
import { Loader } from '@/shared/components/Loader/Loader';

export function CreateExpensePage() {
  const { id } = useParams<{ id: string }>();
  const propertyId = id ?? '';
  const currentUser = useCurrentUser();
  const financialAccounts = useFinancialAccounts(propertyId);

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
          to={`/property-mngt/properties/${propertyId}/accounting/expenses`}
          className="text-sm text-gray-500 hover:underline"
        >
          ← Retour aux dépenses
        </Link>
        <h1 className="text-lg font-semibold text-gray-900">Nouvelle dépense</h1>
      </div>
      <Card className="max-w-2xl">
        <RecordExpenseForm propertyId={propertyId} accounts={financialAccounts.data ?? []} />
      </Card>
    </div>
  );
}
