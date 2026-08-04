import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { Input } from '@/shared/components/Input/Input';
import { Button } from '@/shared/components/Button/Button';
import { Alert } from '@/shared/components/Alert/Alert';
import { getErrorMessage } from '@/shared/utils/getErrorMessage';
import { useUpdateUnitShares } from '@/features/property-mngt/properties/hooks/useUpdateUnitShares';
import {
  updateUnitSharesSchema,
  type UpdateUnitSharesFormValues,
} from '@/features/property-mngt/properties/schemas/updateUnitSharesSchema';
import type { Unit } from '@/features/property-mngt/properties/types/property.types';

interface EditUnitSharesFormProps {
  unit: Unit;
  onSuccess: () => void;
  onCancel: () => void;
}

export function EditUnitSharesForm({ unit, onSuccess, onCancel }: EditUnitSharesFormProps) {
  const {
    register,
    handleSubmit,
    formState: { errors },
  } = useForm<UpdateUnitSharesFormValues>({
    resolver: zodResolver(updateUnitSharesSchema),
    defaultValues: { shares: unit.shares },
  });
  const { mutate, isPending, error } = useUpdateUnitShares(unit.id);

  function onSubmit(values: UpdateUnitSharesFormValues) {
    mutate(values, { onSuccess });
  }

  return (
    <form onSubmit={handleSubmit(onSubmit)} className="flex flex-col gap-4" noValidate>
      {error && <Alert message={getErrorMessage(error)} />}
      <Input
        label="Tantièmes"
        type="number"
        min={0}
        step="any"
        {...register('shares', { valueAsNumber: true })}
        errorMessage={errors.shares?.message}
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
