import { Link } from 'react-router-dom';
import { AuthLayout } from '@/shared/layouts/AuthLayout';
import { Card } from '@/shared/components/Card/Card';
import { RegisterPropertyAdminForm } from '@/features/identity/register/components/RegisterPropertyAdminForm';
import { useRegisterPropertyManagerAdmin } from '@/features/identity/register/hooks/useRegisterPropertyManagerAdmin';
import { getErrorMessage } from '@/shared/utils/getErrorMessage';
import type { RegisterPropertyAdminFormValues } from '@/features/identity/register/schemas/registerPropertyAdminSchema';

export function RegisterPropertyManagerAdminPage() {
  const { mutate, isPending, isSuccess, error } = useRegisterPropertyManagerAdmin();

  function handleSubmit({ confirmPassword: _confirmPassword, ...values }: RegisterPropertyAdminFormValues) {
    mutate({ ...values, phone: values.phone || undefined });
  }

  if (isSuccess) {
    return (
      <AuthLayout>
        <Card>
          <h2 className="mb-2 text-lg font-semibold text-gray-900">Vérifiez votre boîte mail</h2>
          <p className="text-sm text-gray-600">
            Votre compte et votre première copropriété ont été créés. Un email de confirmation vient de vous
            être envoyé. Cliquez sur le lien qu'il contient pour activer votre compte.
          </p>
          <Link to="/login" className="mt-4 inline-block text-sm font-medium text-gray-900 underline">
            Retour à la connexion
          </Link>
        </Card>
      </AuthLayout>
    );
  }

  return (
    <AuthLayout>
      <Card className="max-w-lg">
        <h2 className="mb-4 text-lg font-semibold text-gray-900">Créer un compte cabinet de syndic</h2>
        <p className="mb-4 text-sm text-gray-600">
          Vous gérez des copropriétés à titre professionnel. Vous pourrez en ajouter d'autres par la suite.
        </p>
        <RegisterPropertyAdminForm
          onSubmit={handleSubmit}
          isSubmitting={isPending}
          errorMessage={error ? getErrorMessage(error) : undefined}
          submitLabel="Créer mon compte et ma première copropriété"
        />
        <p className="mt-4 text-center text-sm text-gray-600">
          Déjà un compte ?{' '}
          <Link to="/login" className="font-medium text-gray-900 underline">
            Se connecter
          </Link>
        </p>
      </Card>
    </AuthLayout>
  );
}
