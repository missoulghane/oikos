import { useEffect } from 'react';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { Input } from '@/shared/components/Input/Input';
import { RadioGroup } from '@/shared/components/RadioGroup/RadioGroup';
import { Button } from '@/shared/components/Button/Button';
import { Alert } from '@/shared/components/Alert/Alert';
import { useAddUnit } from '@/features/property-mngt/properties/hooks/useAddUnit';
import { useUnitTypeDefinitions } from '@/features/property-mngt/properties/hooks/useUnitTypeDefinitions';
import { getErrorMessage } from '@/shared/utils/getErrorMessage';
import { addUnitSchema, type AddUnitFormValues } from '@/features/property-mngt/properties/schemas/addUnitSchema';

interface AddUnitFormProps {
  propertyId: string;
  buildingId: string;
  showShares: boolean;
  onSuccess: () => void;
  onCancel: () => void;
}

export function AddUnitForm({ propertyId, buildingId, showShares, onSuccess, onCancel }: AddUnitFormProps) {
  const {
    register,
    handleSubmit,
    setValue,
    formState: { errors },
  } = useForm<AddUnitFormValues>({
    resolver: zodResolver(addUnitSchema),
    defaultValues: { unitTypeId: '', shares: 0 },
  });
  const { mutate, isPending, error } = useAddUnit(buildingId);
  const unitTypes = useUnitTypeDefinitions(propertyId);

  // Types de lot definis par copropriete (pas un enum): "Appartement" est le
  // cas courant, mais il n'existe pas forcement - d'ou le repli sur le premier
  // type disponible. La liste arrive de facon asynchrone, donc la selection par
  // defaut est posee ici plutot que dans defaultValues. La query renvoie une
  // reference stable, l'effet ne rejoue pas et n'ecrase pas le choix du user.
  const unitTypeOptions = unitTypes.data;
  useEffect(() => {
    if (!unitTypeOptions || unitTypeOptions.length === 0) return;
    const preferred =
      unitTypeOptions.find((unitType) => unitType.name.toLowerCase() === 'appartement') ?? unitTypeOptions[0];
    setValue('unitTypeId', preferred.id);
  }, [unitTypeOptions, setValue]);

  function onSubmit(values: AddUnitFormValues) {
    mutate(values, { onSuccess });
  }

  return (
    <form onSubmit={handleSubmit(onSubmit)} className="flex flex-col gap-4 rounded-lg border border-gray-200 dark:border-gray-800 p-4" noValidate>
      {error && <Alert message={getErrorMessage(error)} />}
      {unitTypes.isError && <Alert message={getErrorMessage(unitTypes.error)} />}
      <Input label="Numéro de lot" {...register('unitNumber')} errorMessage={errors.unitNumber?.message} />
      <RadioGroup
        label="Type de lot"
        {...register('unitTypeId')}
        errorMessage={errors.unitTypeId?.message}
        options={(unitTypeOptions ?? []).map((unitType) => ({ value: unitType.id, label: unitType.name }))}
        disabled={unitTypes.isLoading}
      />
      {showShares ? (
        <Input
          label="Tantièmes"
          type="number"
          min={0}
          step="any"
          {...register('shares', { valueAsNumber: true })}
          errorMessage={errors.shares?.message}
        />
      ) : (
        <input type="hidden" {...register('shares', { valueAsNumber: true })} />
      )}
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
