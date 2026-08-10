import { Link, useNavigate, useSearchParams } from 'react-router-dom';
import { AuthLayout } from '@/shared/layouts/AuthLayout';
import { Card } from '@/shared/components/Card/Card';
import { LoginForm } from '@/features/identity/auth/components/LoginForm';
import { useLogin } from '@/features/identity/auth/hooks/useLogin';
import { getErrorMessage } from '@/shared/utils/getErrorMessage';
import { sanitizeReturnTo } from '@/shared/utils/sanitizeReturnTo';
import type { LoginFormValues } from '@/features/identity/auth/schemas/loginSchema';

export function LoginPage() {
  const navigate = useNavigate();
  const [searchParams] = useSearchParams();
  const returnTo = sanitizeReturnTo(searchParams.get('returnTo'));
  const { mutate, isPending, error } = useLogin();

  function handleSubmit(values: LoginFormValues) {
    // LandingPage ("/") sorts board/manager vs plain-owner accounts into
    // /property-mngt or /property-ownership - no need to duplicate that logic here.
    // returnTo overrides that default when the caller (e.g. an invitation
    // wizard) needs control back instead.
    mutate(values, { onSuccess: () => navigate(returnTo ?? '/', { replace: true }) });
  }

  return (
    <AuthLayout>
      <Card>
        <LoginForm
          onSubmit={handleSubmit}
          isSubmitting={isPending}
          errorMessage={error ? getErrorMessage(error) : undefined}
        />
        <div className="mt-4 flex flex-col items-center gap-1 text-sm text-gray-600 dark:text-gray-400">
          <span>
            Pas encore de compte ?{' '}
            <Link to="/register/user" className="font-medium text-gray-900 dark:text-white/90 underline">
              Créer un compte
            </Link>
          </span>
          <span>
            Vous gérez votre propre copropriété ?{' '}
            <Link to="/register/board-admin" className="font-medium text-gray-900 dark:text-white/90 underline">
              Créer un compte syndic bénévole
            </Link>
          </span>
          <span>
            Vous êtes un cabinet professionnel ?{' '}
            <Link to="/register/manager-admin" className="font-medium text-gray-900 dark:text-white/90 underline">
              Créer un compte cabinet de syndic
            </Link>
          </span>
        </div>
      </Card>
    </AuthLayout>
  );
}
