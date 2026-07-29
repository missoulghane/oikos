import { Link, useSearchParams } from 'react-router-dom';
import { AuthLayout } from '@/shared/layouts/AuthLayout';
import { Card } from '@/shared/components/Card/Card';
import { Alert } from '@/shared/components/Alert/Alert';
import { ActivateAccountForm } from '@/features/identity/register/components/ActivateAccountForm';
import { useActivateAccount } from '@/features/identity/register/hooks/useActivateAccount';
import { getErrorMessage } from '@/shared/utils/getErrorMessage';
import type { ActivateAccountFormValues } from '@/features/identity/register/schemas/activateAccountSchema';

export function ActivateAccountPage() {
  const [searchParams] = useSearchParams();
  const token = searchParams.get('token');
  const { mutate, isPending, isSuccess, error } = useActivateAccount();

  function handleSubmit(values: ActivateAccountFormValues) {
    if (token) {
      mutate({ token, newPassword: values.newPassword });
    }
  }

  return (
    <AuthLayout>
      <Card>
        <h2 className="mb-4 text-lg font-semibold text-gray-900">Activation du compte</h2>
        {!token && <Alert message="Ce lien d'activation est invalide." />}
        {token && isSuccess && (
          <p className="text-sm text-gray-600">Votre compte a bien été activé. Vous pouvez maintenant vous connecter.</p>
        )}
        {token && !isSuccess && (
          <ActivateAccountForm
            onSubmit={handleSubmit}
            isSubmitting={isPending}
            errorMessage={error ? getErrorMessage(error) : undefined}
          />
        )}
        <Link to="/login" className="mt-4 inline-block text-sm font-medium text-gray-900 underline">
          Retour à la connexion
        </Link>
      </Card>
    </AuthLayout>
  );
}
