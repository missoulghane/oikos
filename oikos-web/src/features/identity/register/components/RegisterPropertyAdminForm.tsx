import { Controller, useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { Input } from '@/shared/components/Input/Input';
import { PhoneField } from '@/shared/components/PhoneField/PhoneField';
import { Button } from '@/shared/components/Button/Button';
import { Alert } from '@/shared/components/Alert/Alert';
import {
  registerPropertyAdminSchema,
  type RegisterPropertyAdminFormValues,
} from '@/features/identity/register/schemas/registerPropertyAdminSchema';

interface RegisterPropertyAdminFormProps {
  onSubmit: (values: RegisterPropertyAdminFormValues) => void;
  isSubmitting: boolean;
  errorMessage?: string;
  submitLabel: string;
}

export function RegisterPropertyAdminForm({
  onSubmit,
  isSubmitting,
  errorMessage,
  submitLabel,
}: RegisterPropertyAdminFormProps) {
  const {
    register,
    control,
    handleSubmit,
    formState: { errors },
  } = useForm<RegisterPropertyAdminFormValues>({ resolver: zodResolver(registerPropertyAdminSchema) });

  return (
    <form onSubmit={handleSubmit(onSubmit)} className="flex flex-col gap-4" noValidate>
      {errorMessage && <Alert message={errorMessage} />}
      <fieldset className="flex flex-col gap-4 rounded-lg border border-gray-200 dark:border-gray-800 p-4">
        <legend className="px-1 text-sm font-medium text-gray-700 dark:text-gray-300">Vos informations</legend>
        <Input
          label="Nom complet"
          autoComplete="name"
          {...register('fullName')}
          errorMessage={errors.fullName?.message}
        />
        <Input
          label="Email"
          type="email"
          autoComplete="email"
          {...register('email')}
          errorMessage={errors.email?.message}
        />
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
      </fieldset>
      <fieldset className="flex flex-col gap-4 rounded-lg border border-gray-200 dark:border-gray-800 p-4">
        <legend className="px-1 text-sm font-medium text-gray-700 dark:text-gray-300">Votre copropriété</legend>
        <Input
          label="Nom de la copropriété"
          {...register('propertyName')}
          errorMessage={errors.propertyName?.message}
        />
        <Input
          label="Adresse"
          {...register('propertyAddress')}
          errorMessage={errors.propertyAddress?.message}
        />
      </fieldset>
      <Button type="submit" isLoading={isSubmitting} className="mt-2">
        {submitLabel}
      </Button>
    </form>
  );
}
