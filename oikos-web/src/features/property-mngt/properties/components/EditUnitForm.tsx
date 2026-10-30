import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { Input } from '@/shared/components/Input/Input';
import { Select } from '@/shared/components/Select/Select';
import { Button } from '@/shared/components/Button/Button';
import { Alert } from '@/shared/components/Alert/Alert';
import { getErrorMessage } from '@/shared/utils/getErrorMessage';
import { useUpdateUnit } from '@/features/property-mngt/properties/hooks/useUpdateUnit';
import {
  updateUnitSchema,
  type UpdateUnitFormValues,
} from '@/features/property-mngt/properties/schemas/updateUnitSchema';
import { unitNumberFromDigits } from '@/features/property-mngt/properties/utils/unitNumber';
import { floorLabel } from '@/features/property-mngt/properties/utils/floorLabel';
import type { Building, Unit } from '@/features/property-mngt/properties/types/property.types';

interface EditUnitFormProps {
  unit: Unit;
  building: Building;
  /** Les tantièmes ne se saisissent qu'en mode tantièmes, comme à l'ajout du lot. */
  showShares: boolean;
  onSuccess: () => void;
  onCancel: () => void;
}

/** « N° 32 » enregistré, « 32 » saisi : le préfixe est affiché dans le champ (voir unitNumberFromDigits). */
function digitsOf(unitNumber: string): string {
  return unitNumber.replace(/^N°\s*/, '');
}

/**
 * Corrige un lot : son numéro, son étage, ses tantièmes - mêmes champs et mêmes
 * règles qu'à l'ajout (AddUnitForm). Le type n'y est pas : les montants et le
 * compte du lot en dépendent.
 */
export function EditUnitForm({ unit, building, showShares, onSuccess, onCancel }: EditUnitFormProps) {
  const {
    register,
    handleSubmit,
    formState: { errors },
  } = useForm<UpdateUnitFormValues>({
    resolver: zodResolver(updateUnitSchema(building.floorCount)),
    // Erreurs signalées à la sortie du champ, puis à chaque frappe - comme sur
    // tous les formulaires - plutôt que seulement à la validation.
    mode: 'onTouched',
    defaultValues: { unitNumber: digitsOf(unit.unitNumber), floor: unit.floor, shares: unit.shares },
  });
  const { mutate, isPending, error } = useUpdateUnit(unit.id, building.id);
  const floorOptions = Array.from({ length: building.floorCount + 1 }, (_, floor) => floor);

  function onSubmit(values: UpdateUnitFormValues) {
    mutate({ ...values, unitNumber: unitNumberFromDigits(values.unitNumber) }, { onSuccess });
  }

  return (
    <form onSubmit={handleSubmit(onSubmit)} className="flex flex-col gap-4" noValidate>
      {error && <Alert message={getErrorMessage(error)} />}
      <Input
        label="Numéro de lot"
        required
        prefix="N°"
        inputMode="numeric"
        autoComplete="off"
        {...register('unitNumber')}
        errorMessage={errors.unitNumber?.message}
      />
      <Select
        label="Étage"
        // Même conversion qu'à l'ajout (AddUnitForm) : '' est « non renseigné », 0 le rez-de-chaussée.
        {...register('floor', {
          setValueAs: (value) => (value === '' || value === null || value === undefined ? null : Number(value)),
        })}
        errorMessage={errors.floor?.message}
      >
        <option value="">Non renseigné</option>
        {floorOptions.map((floor) => (
          <option key={floor} value={floor}>
            {floorLabel(floor)}
          </option>
        ))}
      </Select>
      {showShares ? (
        <Input
          label="Tantièmes"
          required
          type="number"
          min={0}
          step="any"
          {...register('shares', { valueAsNumber: true })}
          errorMessage={errors.shares?.message}
        />
      ) : (
        <input type="hidden" {...register('shares', { valueAsNumber: true })} />
      )}
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
