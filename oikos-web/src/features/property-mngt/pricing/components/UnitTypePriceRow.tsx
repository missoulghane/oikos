import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { Input } from '@/shared/components/Input/Input';
import { Button } from '@/shared/components/Button/Button';
import { Alert } from '@/shared/components/Alert/Alert';
import { getErrorMessage } from '@/shared/utils/getErrorMessage';
import { useSetUnitTypePrice } from '@/features/property-mngt/pricing/hooks/useSetUnitTypePrice';
import { useRemoveUnitTypePrice } from '@/features/property-mngt/pricing/hooks/useRemoveUnitTypePrice';
import { useRemoveUnitTypeDefinition } from '@/features/property-mngt/properties/hooks/useRemoveUnitTypeDefinition';
import {
  unitTypePriceSchema,
  type UnitTypePriceFormValues,
} from '@/features/property-mngt/pricing/schemas/unitTypePriceSchema';

interface UnitTypePriceRowProps {
  propertyId: string;
  unitTypeId: string;
  label: string;
  currentPrice: number | undefined;
}

export function UnitTypePriceRow({ propertyId, unitTypeId, label, currentPrice }: UnitTypePriceRowProps) {
  const {
    register,
    handleSubmit,
    formState: { errors },
  } = useForm<UnitTypePriceFormValues>({
    resolver: zodResolver(unitTypePriceSchema),
    defaultValues: { price: currentPrice },
  });
  const setPrice = useSetUnitTypePrice(propertyId);
  const removePrice = useRemoveUnitTypePrice(propertyId);
  const removeType = useRemoveUnitTypeDefinition(propertyId);

  function onSubmit(values: UnitTypePriceFormValues) {
    setPrice.mutate({ unitTypeId, price: values.price });
  }

  const error = setPrice.error ?? removePrice.error ?? removeType.error;

  return (
    <form
      onSubmit={handleSubmit(onSubmit)}
      className="flex flex-col gap-2 border-b border-gray-200 dark:border-gray-800 py-3 last:border-b-0 sm:flex-row sm:items-end sm:justify-between"
      noValidate
    >
      <div className="flex flex-col gap-2 sm:flex-row sm:items-end sm:gap-4">
        <p className="w-40 pb-2 text-sm font-medium text-gray-900 dark:text-white/90 sm:pb-0">{label}</p>
        <Input
          label="Prix"
          type="number"
          min={0}
          step="any"
          {...register('price', { valueAsNumber: true })}
          errorMessage={errors.price?.message}
        />
      </div>
      <div className="flex flex-col gap-2">
        <div className="flex gap-2">
          <Button type="submit" isLoading={setPrice.isPending}>
            Enregistrer
          </Button>
          {currentPrice !== undefined && (
            <Button
              type="button"
              variant="secondary"
              isLoading={removePrice.isPending}
              onClick={() => removePrice.mutate(unitTypeId)}
            >
              Retirer le prix
            </Button>
          )}
          <Button
            type="button"
            variant="secondary"
            isLoading={removeType.isPending}
            onClick={() => removeType.mutate(unitTypeId)}
          >
            Supprimer le type
          </Button>
        </div>
        {error && <Alert message={getErrorMessage(error)} />}
      </div>
    </form>
  );
}
