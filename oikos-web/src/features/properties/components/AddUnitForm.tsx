import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { Input } from '@/shared/components/Input/Input';
import { Select } from '@/shared/components/Select/Select';
import { Button } from '@/shared/components/Button/Button';
import { Alert } from '@/shared/components/Alert/Alert';
import { useAddUnit } from '@/features/properties/hooks/useAddUnit';
import { UNIT_TYPE_LABELS } from '@/features/properties/constants/unitTypeLabels';
import { getErrorMessage } from '@/shared/utils/getErrorMessage';
import { UNIT_TYPES } from '@/features/properties/types/property.types';
import { addUnitSchema, type AddUnitFormValues } from '@/features/properties/schemas/addUnitSchema';

interface AddUnitFormProps {
  buildingId: string;
  onSuccess: () => void;
  onCancel: () => void;
}

export function AddUnitForm({ buildingId, onSuccess, onCancel }: AddUnitFormProps) {
  const {
    register,
    handleSubmit,
    formState: { errors },
  } = useForm<AddUnitFormValues>({ resolver: zodResolver(addUnitSchema) });
  const { mutate, isPending, error } = useAddUnit(buildingId);

  function onSubmit(values: AddUnitFormValues) {
    mutate(values, { onSuccess });
  }

  return (
    <form onSubmit={handleSubmit(onSubmit)} className="flex flex-col gap-4 rounded-md border border-slate-200 p-4" noValidate>
      {error && <Alert message={getErrorMessage(error)} />}
      <Input label="Numéro de lot" {...register('unitNumber')} errorMessage={errors.unitNumber?.message} />
      <Select label="Type de lot" {...register('unitType')} errorMessage={errors.unitType?.message} defaultValue="">
        <option value="" disabled>
          Sélectionner un type
        </option>
        {UNIT_TYPES.map((type) => (
          <option key={type} value={type}>
            {UNIT_TYPE_LABELS[type]}
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
