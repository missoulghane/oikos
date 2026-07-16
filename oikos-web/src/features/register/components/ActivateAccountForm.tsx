import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { Input } from '@/shared/components/Input/Input';
import { Button } from '@/shared/components/Button/Button';
import { Alert } from '@/shared/components/Alert/Alert';
import {
  activateAccountSchema,
  type ActivateAccountFormValues,
} from '@/features/register/schemas/activateAccountSchema';

interface ActivateAccountFormProps {
  onSubmit: (values: ActivateAccountFormValues) => void;
  isSubmitting: boolean;
  errorMessage?: string;
}

export function ActivateAccountForm({ onSubmit, isSubmitting, errorMessage }: ActivateAccountFormProps) {
  const {
    register,
    handleSubmit,
    formState: { errors },
  } = useForm<ActivateAccountFormValues>({ resolver: zodResolver(activateAccountSchema) });

  return (
    <form onSubmit={handleSubmit(onSubmit)} className="flex flex-col gap-4" noValidate>
      {errorMessage && <Alert message={errorMessage} />}
      <Input
        label="Nouveau mot de passe"
        type="password"
        autoComplete="new-password"
        {...register('newPassword')}
        errorMessage={errors.newPassword?.message}
      />
      <Button type="submit" isLoading={isSubmitting} className="mt-2">
        Activer mon compte
      </Button>
    </form>
  );
}
