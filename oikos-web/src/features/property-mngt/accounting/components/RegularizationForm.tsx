import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { Input } from '@/shared/components/Input/Input';
import { Select } from '@/shared/components/Select/Select';
import { Button } from '@/shared/components/Button/Button';
import { Alert } from '@/shared/components/Alert/Alert';
import { getErrorMessage } from '@/shared/utils/getErrorMessage';
import { useRecordUnitAccountRegularization } from '@/features/property-mngt/accounting/hooks/useRecordUnitAccountRegularization';
import { UNIT_ACCOUNT_MOVEMENT_DIRECTION_LABELS } from '@/features/property-mngt/accounting/constants/unitAccountMovementLabels';
import {
  regularizationSchema,
  type RegularizationFormValues,
} from '@/features/property-mngt/accounting/schemas/regularizationSchema';

interface RegularizationFormProps {
  propertyId: string;
  unitId: string;
}

export function RegularizationForm({ propertyId, unitId }: RegularizationFormProps) {
  const {
    register,
    handleSubmit,
    reset,
    formState: { errors },
  } = useForm<RegularizationFormValues>({ resolver: zodResolver(regularizationSchema) });
  const { mutate, isPending, error } = useRecordUnitAccountRegularization(propertyId, unitId);

  function onSubmit(values: RegularizationFormValues) {
    mutate(values, { onSuccess: () => reset() });
  }

  return (
    <form onSubmit={handleSubmit(onSubmit)} className="flex flex-col gap-4" noValidate>
      {error && <Alert message={getErrorMessage(error)} />}
      <div className="flex flex-col gap-2 sm:flex-row sm:items-end sm:flex-wrap">
        <Input
          label="Montant"
          type="number"
          min={0}
          step="any"
          {...register('amount', { valueAsNumber: true })}
          errorMessage={errors.amount?.message}
        />
        <Select label="Sens" {...register('direction')} errorMessage={errors.direction?.message}>
          <option value="">Sélectionner…</option>
          {Object.entries(UNIT_ACCOUNT_MOVEMENT_DIRECTION_LABELS).map(([value, label]) => (
            <option key={value} value={value}>
              {label}
            </option>
          ))}
        </Select>
        <Input label="Libellé" {...register('label')} errorMessage={errors.label?.message} />
        <Input label="Motif" {...register('reason')} errorMessage={errors.reason?.message} />
        <Button type="submit" isLoading={isPending}>
          Enregistrer la régularisation
        </Button>
      </div>
    </form>
  );
}
