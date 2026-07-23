import { Link } from 'react-router-dom';
import { AuthLayout } from '@/shared/layouts/AuthLayout';
import { Card } from '@/shared/components/Card/Card';
import { RegisterPropertyManagerForm } from '@/features/identity/register/components/RegisterPropertyManagerForm';
import { useRegisterPropertyManager } from '@/features/identity/register/hooks/useRegisterPropertyManager';
import { getErrorMessage } from '@/shared/utils/getErrorMessage';
import type { RegisterPropertyManagerFormValues } from '@/features/identity/register/schemas/registerPropertyManagerSchema';

export function RegisterPropertyManagerPage() {
  const { mutate, isPending, isSuccess, error } = useRegisterPropertyManager();

  function handleSubmit(values: RegisterPropertyManagerFormValues) {
    mutate({ ...values, phone: values.phone || undefined });
  }

  if (isSuccess) {
    return (
      <AuthLayout>
        <Card>
          <h2 className="mb-2 text-lg font-semibold text-slate-900">Vérifiez votre boîte mail</h2>
          <p className="text-sm text-slate-600">
            Votre compte et votre copropriété ont été créés. Un email de confirmation vient de vous être envoyé.
            Cliquez sur le lien qu'il contient pour activer votre compte.
          </p>
          <Link to="/login" className="mt-4 inline-block text-sm font-medium text-slate-900 underline">
            Retour à la connexion
          </Link>
        </Card>
      </AuthLayout>
    );
  }

  return (
    <AuthLayout>
      <Card className="max-w-lg">
        <h2 className="mb-4 text-lg font-semibold text-slate-900">Créer un compte gestionnaire de copropriété</h2>
        <RegisterPropertyManagerForm
          onSubmit={handleSubmit}
          isSubmitting={isPending}
          errorMessage={error ? getErrorMessage(error) : undefined}
        />
        <p className="mt-4 text-center text-sm text-slate-600">
          Déjà un compte ?{' '}
          <Link to="/login" className="font-medium text-slate-900 underline">
            Se connecter
          </Link>
        </p>
      </Card>
    </AuthLayout>
  );
}
