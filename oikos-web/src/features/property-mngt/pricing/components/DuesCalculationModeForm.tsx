import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { Select } from '@/shared/components/Select/Select';
import { Button } from '@/shared/components/Button/Button';
import { Alert } from '@/shared/components/Alert/Alert';
import { getErrorMessage } from '@/shared/utils/getErrorMessage';
import { useUpdateDuesCalculationMode } from '@/features/property-mngt/pricing/hooks/useUpdateDuesCalculationMode';
import { DUES_CALCULATION_MODE_LABELS } from '@/features/property-mngt/properties/constants/duesCalculationModeLabels';
import {
  duesCalculationModeSchema,
  type DuesCalculationModeFormValues,
} from '@/features/property-mngt/pricing/schemas/duesCalculationModeSchema';
import type { DuesCalculationMode } from '@/features/property-mngt/properties/types/property.types';

interface DuesCalculationModeFormProps {
  propertyId: string;
  currentMode: DuesCalculationMode;
}

export function DuesCalculationModeForm({ propertyId, currentMode }: DuesCalculationModeFormProps) {
  const {
    register,
    handleSubmit,
    formState: { errors },
  } = useForm<DuesCalculationModeFormValues>({
    resolver: zodResolver(duesCalculationModeSchema),
    defaultValues: { mode: currentMode },
  });
  const { mutate, isPending, error } = useUpdateDuesCalculationMode(propertyId);

  function onSubmit(values: DuesCalculationModeFormValues) {
    mutate(values.mode);
  }

  return (
    <form onSubmit={handleSubmit(onSubmit)} className="flex flex-col gap-4" noValidate>
      {error && <Alert message={getErrorMessage(error)} />}
      <div className="flex flex-col gap-2 sm:flex-row sm:items-end">
        <Select label="Mode de gestion des fonds" {...register('mode')} errorMessage={errors.mode?.message}>
          {Object.entries(DUES_CALCULATION_MODE_LABELS).map(([value, label]) => (
            <option key={value} value={value}>
              {label}
            </option>
          ))}
        </Select>
        <Button type="submit" isLoading={isPending}>
          Enregistrer
        </Button>
      </div>
    </form>
  );
}
