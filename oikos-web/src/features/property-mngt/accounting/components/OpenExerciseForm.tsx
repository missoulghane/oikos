import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { Input } from '@/shared/components/Input/Input';
import { Button } from '@/shared/components/Button/Button';
import { Alert } from '@/shared/components/Alert/Alert';
import { getErrorMessage } from '@/shared/utils/getErrorMessage';
import { useOpenAccountingExercise } from '@/features/property-mngt/accounting/hooks/useOpenAccountingExercise';
import {
  openExerciseSchema,
  type OpenExerciseFormValues,
} from '@/features/property-mngt/accounting/schemas/openExerciseSchema';

export function OpenExerciseForm({ propertyId }: { propertyId: string }) {
  const {
    register,
    handleSubmit,
    formState: { errors },
  } = useForm<OpenExerciseFormValues>({ resolver: zodResolver(openExerciseSchema) });
  const { mutate, isPending, error } = useOpenAccountingExercise(propertyId);

  function onSubmit(values: OpenExerciseFormValues) {
    mutate(values);
  }

  return (
    <form onSubmit={handleSubmit(onSubmit)} className="flex flex-col gap-4" noValidate>
      {error && <Alert message={getErrorMessage(error)} />}
      <div className="flex flex-col gap-2 sm:flex-row sm:items-end sm:flex-wrap">
        <Input label="Libellé" {...register('label')} errorMessage={errors.label?.message} />
        <Input label="Date de début" type="date" {...register('startDate')} errorMessage={errors.startDate?.message} />
        <Input label="Date de fin" type="date" {...register('endDate')} errorMessage={errors.endDate?.message} />
        <Input label="Commentaire" {...register('comment')} errorMessage={errors.comment?.message} />
        <Button type="submit" isLoading={isPending}>
          Ouvrir l'exercice
        </Button>
      </div>
    </form>
  );
}
