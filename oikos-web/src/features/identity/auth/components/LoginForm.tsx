import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { Input } from '@/shared/components/Input/Input';
import { Button } from '@/shared/components/Button/Button';
import { Alert } from '@/shared/components/Alert/Alert';
import { loginSchema, type LoginFormValues } from '@/features/identity/auth/schemas/loginSchema';

interface LoginFormProps {
  onSubmit: (values: LoginFormValues) => void;
  isSubmitting: boolean;
  errorMessage?: string;
}

export function LoginForm({ onSubmit, isSubmitting, errorMessage }: LoginFormProps) {
  const {
    register,
    handleSubmit,
    formState: { errors },
  } = useForm<LoginFormValues>({ resolver: zodResolver(loginSchema) });

  return (
    <form onSubmit={handleSubmit(onSubmit)} className="flex flex-col gap-4" noValidate>
      {errorMessage && <Alert message={errorMessage} />}
      <Input
        label="Identifiant"
        autoComplete="username"
        {...register('identifier')}
        errorMessage={errors.identifier?.message}
      />
      <Input
        label="Mot de passe"
        type="password"
        autoComplete="current-password"
        {...register('password')}
        errorMessage={errors.password?.message}
      />
      <Button type="submit" isLoading={isSubmitting} className="mt-2">
        Se connecter
      </Button>
    </form>
  );
}
