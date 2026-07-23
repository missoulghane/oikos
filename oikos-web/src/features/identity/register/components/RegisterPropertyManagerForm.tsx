import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { Input } from '@/shared/components/Input/Input';
import { Button } from '@/shared/components/Button/Button';
import { Alert } from '@/shared/components/Alert/Alert';
import {
  registerPropertyManagerSchema,
  type RegisterPropertyManagerFormValues,
} from '@/features/identity/register/schemas/registerPropertyManagerSchema';

interface RegisterPropertyManagerFormProps {
  onSubmit: (values: RegisterPropertyManagerFormValues) => void;
  isSubmitting: boolean;
  errorMessage?: string;
}

export function RegisterPropertyManagerForm({
  onSubmit,
  isSubmitting,
  errorMessage,
}: RegisterPropertyManagerFormProps) {
  const {
    register,
    handleSubmit,
    formState: { errors },
  } = useForm<RegisterPropertyManagerFormValues>({ resolver: zodResolver(registerPropertyManagerSchema) });

  return (
    <form onSubmit={handleSubmit(onSubmit)} className="flex flex-col gap-4" noValidate>
      {errorMessage && <Alert message={errorMessage} />}
      <fieldset className="flex flex-col gap-4 rounded-md border border-slate-200 p-4">
        <legend className="px-1 text-sm font-medium text-slate-700">Vos informations</legend>
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
        <Input label="Téléphone" autoComplete="tel" {...register('phone')} errorMessage={errors.phone?.message} />
        <Input
          label="Mot de passe"
          type="password"
          autoComplete="new-password"
          {...register('password')}
          errorMessage={errors.password?.message}
        />
      </fieldset>
      <fieldset className="flex flex-col gap-4 rounded-md border border-slate-200 p-4">
        <legend className="px-1 text-sm font-medium text-slate-700">Votre copropriété</legend>
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
        Créer mon compte et ma copropriété
      </Button>
    </form>
  );
}
