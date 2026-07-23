import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { Input } from '@/shared/components/Input/Input';
import { Button } from '@/shared/components/Button/Button';
import { Alert } from '@/shared/components/Alert/Alert';
import { getErrorMessage } from '@/shared/utils/getErrorMessage';
import { useUpdateProperty } from '@/features/property-mngt/properties/hooks/useUpdateProperty';
import {
  updatePropertySchema,
  type UpdatePropertyFormValues,
} from '@/features/property-mngt/properties/schemas/updatePropertySchema';
import type { Property } from '@/features/property-mngt/properties/types/property.types';

interface EditPropertyInfoFormProps {
  property: Property;
  onSuccess: () => void;
  onCancel: () => void;
}

export function EditPropertyInfoForm({ property, onSuccess, onCancel }: EditPropertyInfoFormProps) {
  const {
    register,
    handleSubmit,
    formState: { errors },
  } = useForm<UpdatePropertyFormValues>({
    resolver: zodResolver(updatePropertySchema),
    defaultValues: { name: property.name, address: property.address },
  });
  const { mutate, isPending, error } = useUpdateProperty(property.id);

  function onSubmit(values: UpdatePropertyFormValues) {
    mutate(values, { onSuccess });
  }

  return (
    <form onSubmit={handleSubmit(onSubmit)} className="flex flex-col gap-4" noValidate>
      {error && <Alert message={getErrorMessage(error)} />}
      <Input label="Nom de la copropriété" {...register('name')} errorMessage={errors.name?.message} />
      <Input label="Adresse" {...register('address')} errorMessage={errors.address?.message} />
      <div className="flex gap-2">
        <Button type="submit" isLoading={isPending}>
          Enregistrer
        </Button>
        <Button type="button" variant="secondary" onClick={onCancel}>
          Annuler
        </Button>
      </div>
    </form>
  );
}
