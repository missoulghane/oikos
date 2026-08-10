import { Link } from 'react-router-dom';
import { AuthLayout } from '@/shared/layouts/AuthLayout';
import { Card } from '@/shared/components/Card/Card';
import { RegisterPropertyAdminForm } from '@/features/identity/register/components/RegisterPropertyAdminForm';
import { useRegisterPropertyBoardAdmin } from '@/features/identity/register/hooks/useRegisterPropertyBoardAdmin';
import { getErrorMessage } from '@/shared/utils/getErrorMessage';
import type { RegisterPropertyAdminFormValues } from '@/features/identity/register/schemas/registerPropertyAdminSchema';

export function RegisterPropertyBoardAdminPage() {
  const { mutate, isPending, isSuccess, error } = useRegisterPropertyBoardAdmin();

  function handleSubmit({ confirmPassword: _confirmPassword, ...values }: RegisterPropertyAdminFormValues) {
    mutate({ ...values, phone: values.phone || undefined });
  }

  if (isSuccess) {
    return (
      <AuthLayout>
        <Card>
          <h2 className="mb-2 text-lg font-semibold text-gray-900 dark:text-white/90">Vérifiez votre boîte mail</h2>
          <p className="text-sm text-gray-600 dark:text-gray-400">
            Votre compte et votre copropriété ont été créés. Un email de confirmation vient de vous être envoyé.
            Cliquez sur le lien qu'il contient pour activer votre compte.
          </p>
          <Link to="/login" className="mt-4 inline-block text-sm font-medium text-gray-900 dark:text-white/90 underline">
            Retour à la connexion
          </Link>
        </Card>
      </AuthLayout>
    );
  }

  return (
    <AuthLayout>
      <Card className="max-w-lg">
        <h2 className="mb-4 text-lg font-semibold text-gray-900 dark:text-white/90">Créer un compte syndic bénévole</h2>
        <p className="mb-4 text-sm text-gray-600 dark:text-gray-400">
          Vous gérez vous-même votre copropriété. Votre compte sera limité à cette seule copropriété.
        </p>
        <RegisterPropertyAdminForm
          onSubmit={handleSubmit}
          isSubmitting={isPending}
          errorMessage={error ? getErrorMessage(error) : undefined}
          submitLabel="Créer mon compte et ma copropriété"
        />
        <p className="mt-4 text-center text-sm text-gray-600 dark:text-gray-400">
          Déjà un compte ?{' '}
          <Link to="/login" className="font-medium text-gray-900 dark:text-white/90 underline">
            Se connecter
          </Link>
        </p>
      </Card>
    </AuthLayout>
  );
}
