import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { Input } from '@/shared/components/Input/Input';
import { Button } from '@/shared/components/Button/Button';
import { Alert } from '@/shared/components/Alert/Alert';
import { getErrorMessage } from '@/shared/utils/getErrorMessage';
import { useUpdateBuilding } from '@/features/property-mngt/properties/hooks/useUpdateBuilding';
import {
  updateBuildingSchema,
  type UpdateBuildingFormValues,
} from '@/features/property-mngt/properties/schemas/updateBuildingSchema';
import type { Building } from '@/features/property-mngt/properties/types/property.types';

interface EditBuildingFormProps {
  building: Building;
  onSuccess: () => void;
  onCancel: () => void;
}

export function EditBuildingForm({ building, onSuccess, onCancel }: EditBuildingFormProps) {
  const {
    register,
    handleSubmit,
    formState: { errors },
  } = useForm<UpdateBuildingFormValues>({
    resolver: zodResolver(updateBuildingSchema),
    defaultValues: { name: building.name, floorCount: building.floorCount },
  });
  const { mutate, isPending, error } = useUpdateBuilding(building.propertyId, building.id);

  function onSubmit(values: UpdateBuildingFormValues) {
    mutate(values, { onSuccess });
  }

  return (
    <form onSubmit={handleSubmit(onSubmit)} className="flex flex-col gap-4" noValidate>
      {error && <Alert message={getErrorMessage(error)} />}
      <Input label="Nom de l'immeuble" {...register('name')} errorMessage={errors.name?.message} />
      <Input
        label="Nombre d'étages"
        type="number"
        min={0}
        // valueAsNumber : sans lui le champ rend une chaîne, que le schéma
        // refuse - et l'erreur affichée parlerait de type là où la saisie est
        // parfaitement valide.
        {...register('floorCount', { valueAsNumber: true })}
        errorMessage={errors.floorCount?.message}
      />
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
