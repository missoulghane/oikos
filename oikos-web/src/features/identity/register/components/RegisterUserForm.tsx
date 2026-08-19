import { Controller, useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { Input } from '@/shared/components/Input/Input';
import { PhoneField } from '@/shared/components/PhoneField/PhoneField';
import { Button } from '@/shared/components/Button/Button';
import { Alert } from '@/shared/components/Alert/Alert';
import { registerUserSchema, type RegisterUserFormValues } from '@/features/identity/register/schemas/registerUserSchema';

interface RegisterUserFormProps {
  onSubmit: (values: RegisterUserFormValues) => void;
  isSubmitting: boolean;
  errorMessage?: string;
}

export function RegisterUserForm({ onSubmit, isSubmitting, errorMessage }: RegisterUserFormProps) {
  const {
    register,
    control,
    handleSubmit,
    formState: { errors },
  } = useForm<RegisterUserFormValues>({ resolver: zodResolver(registerUserSchema) });

  return (
    <form onSubmit={handleSubmit(onSubmit)} className="flex flex-col gap-4" noValidate>
      {errorMessage && <Alert message={errorMessage} />}
      <Input
        label="Nom complet"
        autoComplete="name"
        {...register('fullName')}
        errorMessage={errors.fullName?.message}
      />
      <Input label="Email" type="email" autoComplete="email" {...register('email')} errorMessage={errors.email?.message} />
      <Controller
        control={control}
        name="phone"
        defaultValue=""
        render={({ field }) => (
          <PhoneField
            label="Téléphone"
            name={field.name}
            value={field.value}
            onChange={field.onChange}
            onBlur={field.onBlur}
            errorMessage={errors.phone?.message}
          />
        )}
      />
      <Input
        label="Mot de passe"
        type="password"
        autoComplete="new-password"
        {...register('password')}
        errorMessage={errors.password?.message}
      />
      <Input
        label="Confirmer le mot de passe"
        type="password"
        autoComplete="new-password"
        {...register('confirmPassword')}
        errorMessage={errors.confirmPassword?.message}
      />
      <Button type="submit" isLoading={isSubmitting} className="mt-2">
        Créer mon compte
      </Button>
    </form>
  );
}
