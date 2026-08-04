import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { Input } from '@/shared/components/Input/Input';
import { Button } from '@/shared/components/Button/Button';
import { Alert } from '@/shared/components/Alert/Alert';
import { getErrorMessage } from '@/shared/utils/getErrorMessage';
import { useSetProjectedBudget } from '@/features/property-mngt/pricing/hooks/useSetProjectedBudget';
import {
  projectedBudgetSchema,
  type ProjectedBudgetFormValues,
} from '@/features/property-mngt/pricing/schemas/projectedBudgetSchema';

interface ProjectedBudgetFormProps {
  propertyId: string;
  currentProjectedBudget: number | null;
}

export function ProjectedBudgetForm({ propertyId, currentProjectedBudget }: ProjectedBudgetFormProps) {
  const {
    register,
    handleSubmit,
    formState: { errors },
  } = useForm<ProjectedBudgetFormValues>({
    resolver: zodResolver(projectedBudgetSchema),
    defaultValues: { projectedBudget: currentProjectedBudget ?? undefined },
  });
  const { mutate, isPending, error } = useSetProjectedBudget(propertyId);

  function onSubmit(values: ProjectedBudgetFormValues) {
    mutate(values.projectedBudget);
  }

  return (
    <form onSubmit={handleSubmit(onSubmit)} className="flex flex-col gap-4" noValidate>
      {error && <Alert message={getErrorMessage(error)} />}
      <div className="flex flex-col gap-2 sm:flex-row sm:items-end">
        <Input
          label="Budget prévisionnel"
          type="number"
          min={0}
          step="any"
          {...register('projectedBudget', { valueAsNumber: true })}
          errorMessage={errors.projectedBudget?.message}
        />
        <Button type="submit" isLoading={isPending}>
          Enregistrer
        </Button>
      </div>
    </form>
  );
}
