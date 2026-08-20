import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { Input } from '@/shared/components/Input/Input';
import { Button } from '@/shared/components/Button/Button';
import { Alert } from '@/shared/components/Alert/Alert';
import {
  createPropertySchema,
  type CreatePropertyFormValues,
} from '@/features/property-mngt/properties/schemas/createPropertySchema';

interface CreatePropertyFormProps {
  onSubmit: (values: CreatePropertyFormValues) => void;
  isSubmitting: boolean;
  errorMessage?: string;
}

export function CreatePropertyForm({ onSubmit, isSubmitting, errorMessage }: CreatePropertyFormProps) {
  const {
    register,
    handleSubmit,
    formState: { errors },
  } = useForm<CreatePropertyFormValues>({ resolver: zodResolver(createPropertySchema) });

  return (
    <form onSubmit={handleSubmit(onSubmit)} className="flex flex-col gap-4" noValidate>
      {errorMessage && <Alert message={errorMessage} />}
      <Input label="Nom de la copropriété" {...register('name')} errorMessage={errors.name?.message} />
      <Input label="Adresse" {...register('address')} errorMessage={errors.address?.message} />
      <Input label="Ville" {...register('city')} errorMessage={errors.city?.message} />
      <Button type="submit" isLoading={isSubmitting} className="mt-2">
        Créer la copropriété
      </Button>
    </form>
  );
}
