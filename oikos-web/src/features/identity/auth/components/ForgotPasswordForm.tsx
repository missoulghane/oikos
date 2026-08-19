import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { Input } from '@/shared/components/Input/Input';
import { Button } from '@/shared/components/Button/Button';
import { Alert } from '@/shared/components/Alert/Alert';
import {
  forgotPasswordSchema,
  type ForgotPasswordFormValues,
} from '@/features/identity/auth/schemas/forgotPasswordSchema';

interface ForgotPasswordFormProps {
  onSubmit: (values: ForgotPasswordFormValues) => void;
  isSubmitting: boolean;
  errorMessage?: string;
}

export function ForgotPasswordForm({ onSubmit, isSubmitting, errorMessage }: ForgotPasswordFormProps) {
  const {
    register,
    handleSubmit,
    formState: { errors },
  } = useForm<ForgotPasswordFormValues>({ resolver: zodResolver(forgotPasswordSchema) });

  return (
    <form onSubmit={handleSubmit(onSubmit)} className="flex flex-col gap-4" noValidate>
      {errorMessage && <Alert message={errorMessage} />}
      <Input label="Adresse email" type="email" autoComplete="email" {...register('email')} errorMessage={errors.email?.message} />
      <Button type="submit" isLoading={isSubmitting} className="mt-2">
        Envoyer le lien
      </Button>
    </form>
  );
}
