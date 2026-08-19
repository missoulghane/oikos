import { Link } from 'react-router-dom';
import { AuthLayout } from '@/shared/layouts/AuthLayout';
import { Card } from '@/shared/components/Card/Card';
import { ForgotPasswordForm } from '@/features/identity/auth/components/ForgotPasswordForm';
import { useForgotPassword } from '@/features/identity/auth/hooks/useForgotPassword';
import { getErrorMessage } from '@/shared/utils/getErrorMessage';
import type { ForgotPasswordFormValues } from '@/features/identity/auth/schemas/forgotPasswordSchema';

export function ForgotPasswordPage() {
  const { mutate, isPending, isSuccess, error } = useForgotPassword();

  function handleSubmit(values: ForgotPasswordFormValues) {
    mutate(values);
  }

  // L'écran de confirmation ne dit pas si l'adresse est connue, et c'est
  // délibéré : l'API répond 202 dans les deux cas pour ne pas se transformer en
  // annuaire des comptes (RequestPasswordResetService). Un « cette adresse est
  // inconnue » ici annulerait la précaution côté serveur.
  if (isSuccess) {
    return (
      <AuthLayout>
        <Card>
          <h2 className="mb-4 text-lg font-semibold text-gray-900 dark:text-white/90">Vérifiez votre boîte mail</h2>
          <p className="text-sm text-gray-600 dark:text-gray-400">
            Si un compte existe pour cette adresse, un email contenant un lien de réinitialisation vient de vous être
            envoyé. Ce lien est valable une heure.
          </p>
          <Link to="/login" className="mt-4 inline-block text-sm font-medium text-gray-900 underline dark:text-white/90">
            Retour à la connexion
          </Link>
        </Card>
      </AuthLayout>
    );
  }

  return (
    <AuthLayout>
      <Card>
        <h2 className="mb-2 text-lg font-semibold text-gray-900 dark:text-white/90">Mot de passe oublié</h2>
        <p className="mb-4 text-sm text-gray-600 dark:text-gray-400">
          Indiquez l'adresse email de votre compte : nous vous enverrons un lien pour choisir un nouveau mot de passe.
        </p>
        <ForgotPasswordForm
          onSubmit={handleSubmit}
          isSubmitting={isPending}
          errorMessage={error ? getErrorMessage(error) : undefined}
        />
        <Link to="/login" className="mt-4 inline-block text-sm font-medium text-gray-900 underline dark:text-white/90">
          Retour à la connexion
        </Link>
      </Card>
    </AuthLayout>
  );
}
