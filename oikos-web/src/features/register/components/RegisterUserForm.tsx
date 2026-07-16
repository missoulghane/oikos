import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { Input } from '@/shared/components/Input/Input';
import { Button } from '@/shared/components/Button/Button';
import { Alert } from '@/shared/components/Alert/Alert';
import { registerUserSchema, type RegisterUserFormValues } from '@/features/register/schemas/registerUserSchema';

interface RegisterUserFormProps {
  onSubmit: (values: RegisterUserFormValues) => void;
  isSubmitting: boolean;
  errorMessage?: string;
}

export function RegisterUserForm({ onSubmit, isSubmitting, errorMessage }: RegisterUserFormProps) {
  const {
    register,
    handleSubmit,
    formState: { errors },
  } = useForm<RegisterUserFormValues>({ resolver: zodResolver(registerUserSchema) });

  return (
    <form onSubmit={handleSubmit(onSubmit)} className="flex flex-col gap-4" noValidate>
      {errorMessage && <Alert message={errorMessage} />}
      <Input label="Nom" autoComplete="family-name" {...register('lastName')} errorMessage={errors.lastName?.message} />
      <Input
        label="Prénom"
        autoComplete="given-name"
        {...register('firstName')}
        errorMessage={errors.firstName?.message}
      />
      <Input label="Email" type="email" autoComplete="email" {...register('email')} errorMessage={errors.email?.message} />
      <Input label="Téléphone" autoComplete="tel" {...register('phone')} errorMessage={errors.phone?.message} />
      <Input
        label="Mot de passe"
        type="password"
        autoComplete="new-password"
        {...register('password')}
        errorMessage={errors.password?.message}
      />
      <Button type="submit" isLoading={isSubmitting} className="mt-2">
        Créer mon compte
      </Button>
    </form>
  );
}
