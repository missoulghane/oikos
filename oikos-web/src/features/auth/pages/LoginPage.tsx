import { useNavigate } from 'react-router-dom';
import { AuthLayout } from '@/shared/layouts/AuthLayout';
import { Card } from '@/shared/components/Card/Card';
import { LoginForm } from '@/features/auth/components/LoginForm';
import { useLogin } from '@/features/auth/hooks/useLogin';
import { getErrorMessage } from '@/shared/utils/getErrorMessage';
import type { LoginFormValues } from '@/features/auth/schemas/loginSchema';

export function LoginPage() {
  const navigate = useNavigate();
  const { mutate, isPending, error } = useLogin();

  function handleSubmit(values: LoginFormValues) {
    mutate(values, { onSuccess: () => navigate('/properties', { replace: true }) });
  }

  return (
    <AuthLayout>
      <Card>
        <LoginForm
          onSubmit={handleSubmit}
          isSubmitting={isPending}
          errorMessage={error ? getErrorMessage(error) : undefined}
        />
      </Card>
    </AuthLayout>
  );
}
