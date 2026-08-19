import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { Input } from '@/shared/components/Input/Input';
import { Button } from '@/shared/components/Button/Button';
import { Alert } from '@/shared/components/Alert/Alert';
import {
  resetPasswordSchema,
  type ResetPasswordFormValues,
} from '@/features/identity/auth/schemas/resetPasswordSchema';

interface ResetPasswordFormProps {
  onSubmit: (values: ResetPasswordFormValues) => void;
  isSubmitting: boolean;
  errorMessage?: string;
}

export function ResetPasswordForm({ onSubmit, isSubmitting, errorMessage }: ResetPasswordFormProps) {
  const {
    register,
    handleSubmit,
    formState: { errors },
  } = useForm<ResetPasswordFormValues>({ resolver: zodResolver(resetPasswordSchema) });

  return (
    <form onSubmit={handleSubmit(onSubmit)} className="flex flex-col gap-4" noValidate>
      {errorMessage && <Alert message={errorMessage} />}
      <Input
        label="Nouveau mot de passe"
        type="password"
        autoComplete="new-password"
        // deps : corriger le premier champ doit rafraîchir l'erreur de concordance
        // portée par le second, comme dans le wizard d'inscription.
        {...register('password', { deps: ['confirmPassword'] })}
        errorMessage={errors.password?.message}
      />
      <Input
        label="Confirmation du mot de passe"
        type="password"
        autoComplete="new-password"
        {...register('confirmPassword')}
        errorMessage={errors.confirmPassword?.message}
      />
      <Button type="submit" isLoading={isSubmitting} className="mt-2">
        Réinitialiser mon mot de passe
      </Button>
    </form>
  );
}
