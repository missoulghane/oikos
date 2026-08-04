import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { Input } from '@/shared/components/Input/Input';
import { Button } from '@/shared/components/Button/Button';
import { Alert } from '@/shared/components/Alert/Alert';
import {
  invitationSignupSchema,
  type InvitationSignupFormValues,
} from '@/features/identity/invitations/schemas/invitationSignupSchema';

interface InvitationSignupFormProps {
  onSubmit: (values: InvitationSignupFormValues) => void;
  isSubmitting: boolean;
  errorMessage?: string;
  disabled?: boolean;
}

export function InvitationSignupForm({ onSubmit, isSubmitting, errorMessage, disabled }: InvitationSignupFormProps) {
  const {
    register,
    handleSubmit,
    formState: { errors },
  } = useForm<InvitationSignupFormValues>({ resolver: zodResolver(invitationSignupSchema) });

  return (
    <form onSubmit={handleSubmit(onSubmit)} className="flex flex-col gap-4" noValidate>
      {errorMessage && <Alert message={errorMessage} />}
      <Input label="Nom complet" autoComplete="name" {...register('fullName')} errorMessage={errors.fullName?.message} />
      <Input label="Email" type="email" autoComplete="email" {...register('email')} errorMessage={errors.email?.message} />
      <Input
        label="Mot de passe"
        type="password"
        autoComplete="new-password"
        {...register('password')}
        errorMessage={errors.password?.message}
      />
      <Button type="submit" isLoading={isSubmitting} disabled={disabled} className="mt-2">
        Créer mon compte et continuer
      </Button>
    </form>
  );
}
