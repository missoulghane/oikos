import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { Input } from '@/shared/components/Input/Input';
import { Select } from '@/shared/components/Select/Select';
import { Button } from '@/shared/components/Button/Button';
import { Alert } from '@/shared/components/Alert/Alert';
import { useAddUnit } from '@/features/property-mngt/properties/hooks/useAddUnit';
import { useUnitTypeDefinitions } from '@/features/property-mngt/properties/hooks/useUnitTypeDefinitions';
import { getErrorMessage } from '@/shared/utils/getErrorMessage';
import { addUnitSchema, type AddUnitFormValues } from '@/features/property-mngt/properties/schemas/addUnitSchema';

interface AddUnitFormProps {
  propertyId: string;
  buildingId: string;
  onSuccess: () => void;
  onCancel: () => void;
}

export function AddUnitForm({ propertyId, buildingId, onSuccess, onCancel }: AddUnitFormProps) {
  const {
    register,
    handleSubmit,
    formState: { errors },
  } = useForm<AddUnitFormValues>({ resolver: zodResolver(addUnitSchema) });
  const { mutate, isPending, error } = useAddUnit(buildingId);
  const unitTypes = useUnitTypeDefinitions(propertyId);

  function onSubmit(values: AddUnitFormValues) {
    mutate(values, { onSuccess });
  }

  return (
    <form onSubmit={handleSubmit(onSubmit)} className="flex flex-col gap-4 rounded-lg border border-gray-200 p-4" noValidate>
      {error && <Alert message={getErrorMessage(error)} />}
      {unitTypes.isError && <Alert message={getErrorMessage(unitTypes.error)} />}
      <Input label="Numéro de lot" {...register('unitNumber')} errorMessage={errors.unitNumber?.message} />
      <Select
        label="Type de lot"
        {...register('unitTypeId')}
        errorMessage={errors.unitTypeId?.message}
        defaultValue=""
        disabled={unitTypes.isLoading}
      >
        <option value="" disabled>
          Sélectionner un type
        </option>
        {unitTypes.data?.map((unitType) => (
          <option key={unitType.id} value={unitType.id}>
            {unitType.name}
          </option>
        ))}
      </Select>
      <Input
        label="Tantièmes"
        type="number"
        min={0}
        step="any"
        {...register('shares', { valueAsNumber: true })}
        errorMessage={errors.shares?.message}
      />
      <div className="mt-2 flex gap-2">
        <Button type="submit" isLoading={isPending}>
          Ajouter le lot
        </Button>
        <Button type="button" variant="secondary" onClick={onCancel}>
          Annuler
        </Button>
      </div>
    </form>
  );
}
