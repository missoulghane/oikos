import { Link, useSearchParams } from 'react-router-dom';
import { AuthLayout } from '@/shared/layouts/AuthLayout';
import { Card } from '@/shared/components/Card/Card';
import { Alert } from '@/shared/components/Alert/Alert';
import { ResetPasswordForm } from '@/features/identity/auth/components/ResetPasswordForm';
import { useResetPassword } from '@/features/identity/auth/hooks/useResetPassword';
import { getErrorMessage } from '@/shared/utils/getErrorMessage';
import type { ResetPasswordFormValues } from '@/features/identity/auth/schemas/resetPasswordSchema';

/**
 * Cible du lien envoyé par email ; l'adresse est construite côté API à partir de
 * `oikos.mail.password-reset-base-url`, qui pointe sur cette route.
 */
export function ResetPasswordPage() {
  const [searchParams] = useSearchParams();
  const token = searchParams.get('token');
  const { mutate, isPending, isSuccess, error } = useResetPassword();

  function handleSubmit(values: ResetPasswordFormValues) {
    if (token) {
      // Le formulaire nomme ses champs password / confirmPassword (pour réutiliser
      // la vérification de concordance) ; l'API attend newPassword.
      mutate({ token, newPassword: values.password });
    }
  }

  return (
    <AuthLayout>
      <Card>
        <h2 className="mb-4 text-lg font-semibold text-gray-900 dark:text-white/90">Nouveau mot de passe</h2>
        {!token && (
          <>
            <Alert message="Ce lien de réinitialisation est invalide." />
            <p className="mt-3 text-sm text-gray-600 dark:text-gray-400">
              Le lien est peut-être incomplet : les messageries coupent parfois les adresses longues.{' '}
              <Link to="/forgot-password" className="font-medium text-gray-900 underline dark:text-white/90">
                Demandez-en un nouveau
              </Link>
              .
            </p>
          </>
        )}
        {token && isSuccess && (
          <p className="text-sm text-gray-600 dark:text-gray-400">
            Votre mot de passe a bien été réinitialisé. Vos autres appareils ont été déconnectés ; vous pouvez
            maintenant vous connecter.
          </p>
        )}
        {token && !isSuccess && (
          <ResetPasswordForm
            onSubmit={handleSubmit}
            isSubmitting={isPending}
            errorMessage={error ? getErrorMessage(error) : undefined}
          />
        )}
        <Link to="/login" className="mt-4 inline-block text-sm font-medium text-gray-900 underline dark:text-white/90">
          Retour à la connexion
        </Link>
      </Card>
    </AuthLayout>
  );
}
