import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { Input } from '@/shared/components/Input/Input';
import { Button } from '@/shared/components/Button/Button';
import { Alert } from '@/shared/components/Alert/Alert';
import { useAddBuilding } from '@/features/property-mngt/properties/hooks/useAddBuilding';
import { getErrorMessage } from '@/shared/utils/getErrorMessage';
import {
  addBuildingSchema,
  type AddBuildingFormValues,
} from '@/features/property-mngt/properties/schemas/addBuildingSchema';

interface AddBuildingFormProps {
  propertyId: string;
  onSuccess: () => void;
  onCancel: () => void;
}

export function AddBuildingForm({ propertyId, onSuccess, onCancel }: AddBuildingFormProps) {
  const {
    register,
    handleSubmit,
    formState: { errors },
  } = useForm<AddBuildingFormValues>({ resolver: zodResolver(addBuildingSchema) });
  const { mutate, isPending, error } = useAddBuilding(propertyId);

  function onSubmit(values: AddBuildingFormValues) {
    mutate(values, { onSuccess });
  }

  return (
    <form onSubmit={handleSubmit(onSubmit)} className="flex flex-col gap-4 rounded-lg border border-gray-200 dark:border-gray-800 p-4" noValidate>
      {error && <Alert message={getErrorMessage(error)} />}
      <Input label="Nom de l'immeuble" {...register('name')} errorMessage={errors.name?.message} />
      <Input
        label="Nombre d'étages"
        type="number"
        min={0}
        step={1}
        {...register('floorCount', { valueAsNumber: true })}
        errorMessage={errors.floorCount?.message}
      />
      <div className="mt-2 flex gap-2">
        <Button type="submit" isLoading={isPending}>
          Ajouter l'immeuble
        </Button>
        <Button type="button" variant="secondary" onClick={onCancel}>
          Annuler
        </Button>
      </div>
    </form>
  );
}
