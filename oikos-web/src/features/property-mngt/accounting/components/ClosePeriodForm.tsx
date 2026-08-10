import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { Input } from '@/shared/components/Input/Input';
import { Button } from '@/shared/components/Button/Button';
import { Alert } from '@/shared/components/Alert/Alert';
import { getErrorMessage } from '@/shared/utils/getErrorMessage';
import { useClosePeriod } from '@/features/property-mngt/accounting/hooks/useClosePeriod';
import {
  closePeriodSchema,
  type ClosePeriodFormValues,
} from '@/features/property-mngt/accounting/schemas/closePeriodSchema';

export function ClosePeriodForm({ propertyId }: { propertyId: string }) {
  const {
    register,
    handleSubmit,
    reset,
    formState: { errors },
  } = useForm<ClosePeriodFormValues>({ resolver: zodResolver(closePeriodSchema) });
  const { mutate, isPending, isSuccess, data, error } = useClosePeriod(propertyId);

  function onSubmit(values: ClosePeriodFormValues) {
    mutate(values.period, { onSuccess: () => reset() });
  }

  return (
    <form onSubmit={handleSubmit(onSubmit)} className="flex flex-col gap-3" noValidate>
      {error && <Alert message={getErrorMessage(error)} />}
      {isSuccess && data && <Alert variant="success" message={`Période ${data.yearMonth} clôturée.`} />}
      <div className="flex flex-col gap-2 sm:flex-row sm:items-end">
        <Input label="Période à clôturer" type="month" {...register('period')} errorMessage={errors.period?.message} />
        <Button type="submit" variant="secondary" isLoading={isPending}>
          Clôturer la période
        </Button>
      </div>
    </form>
  );
}
