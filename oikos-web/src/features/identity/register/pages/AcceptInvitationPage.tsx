import { Link, useSearchParams } from 'react-router-dom';
import { AuthLayout } from '@/shared/layouts/AuthLayout';
import { Card } from '@/shared/components/Card/Card';
import { Alert } from '@/shared/components/Alert/Alert';
import { AcceptInvitationForm } from '@/features/identity/register/components/AcceptInvitationForm';
import { useAcceptInvitation } from '@/features/identity/register/hooks/useAcceptInvitation';
import { getErrorMessage } from '@/shared/utils/getErrorMessage';
import type { AcceptInvitationFormValues } from '@/features/identity/register/schemas/acceptInvitationSchema';

export function AcceptInvitationPage() {
  const [searchParams] = useSearchParams();
  const token = searchParams.get('token');
  const { mutate, isPending, isSuccess, error } = useAcceptInvitation();

  function handleSubmit(values: AcceptInvitationFormValues) {
    if (token) {
      mutate({ token, password: values.password === '' ? undefined : values.password });
    }
  }

  return (
    <AuthLayout>
      <Card>
        <h2 className="mb-4 text-lg font-semibold text-slate-900">Invitation à rejoindre Oikos</h2>
        {!token && <Alert message="Ce lien d'invitation est invalide." />}
        {token && isSuccess && (
          <p className="text-sm text-slate-600">
            Invitation acceptée. Vous pouvez maintenant vous connecter pour accéder à vos lots.
          </p>
        )}
        {token && !isSuccess && (
          <AcceptInvitationForm
            onSubmit={handleSubmit}
            isSubmitting={isPending}
            errorMessage={error ? getErrorMessage(error) : undefined}
          />
        )}
        <Link to="/login" className="mt-4 inline-block text-sm font-medium text-slate-900 underline">
          Retour à la connexion
        </Link>
      </Card>
    </AuthLayout>
  );
}
