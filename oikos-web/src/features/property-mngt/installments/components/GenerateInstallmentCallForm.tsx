import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { Input } from '@/shared/components/Input/Input';
import { Button } from '@/shared/components/Button/Button';
import { Alert } from '@/shared/components/Alert/Alert';
import { getErrorMessage } from '@/shared/utils/getErrorMessage';
import { useGenerateInstallmentCall } from '@/features/property-mngt/installments/hooks/useGenerateInstallmentCall';
import {
  generateInstallmentCallSchema,
  type GenerateInstallmentCallFormValues,
} from '@/features/property-mngt/installments/schemas/generateInstallmentCallSchema';
import type { GenerateInstallmentCallResult } from '@/features/property-mngt/installments/types/installmentCall.types';

interface GenerateInstallmentCallFormProps {
  propertyId: string;
  onGenerated: (result: GenerateInstallmentCallResult) => void;
}

export function GenerateInstallmentCallForm({ propertyId, onGenerated }: GenerateInstallmentCallFormProps) {
  const {
    register,
    handleSubmit,
    reset,
    formState: { errors },
  } = useForm<GenerateInstallmentCallFormValues>({ resolver: zodResolver(generateInstallmentCallSchema) });
  const { mutate, isPending, error } = useGenerateInstallmentCall(propertyId);

  function onSubmit(values: GenerateInstallmentCallFormValues) {
    mutate(values, {
      onSuccess: (result) => {
        reset();
        onGenerated(result);
      },
    });
  }

  return (
    <form onSubmit={handleSubmit(onSubmit)} className="flex flex-col gap-4" noValidate>
      {error && <Alert message={getErrorMessage(error)} />}
      <div className="flex flex-col gap-2 sm:flex-row sm:items-end">
        <Input label="Période" type="month" {...register('period')} errorMessage={errors.period?.message} />
        <Input
          label="Date d'échéance"
          type="date"
          {...register('dueDate')}
          errorMessage={errors.dueDate?.message}
        />
        <Button type="submit" isLoading={isPending}>
          Générer un appel de fonds
        </Button>
      </div>
    </form>
  );
}
