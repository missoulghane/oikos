import { useEffect, useMemo } from 'react';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { Input } from '@/shared/components/Input/Input';
import { Select } from '@/shared/components/Select/Select';
import { RadioGroup } from '@/shared/components/RadioGroup/RadioGroup';
import { Button } from '@/shared/components/Button/Button';
import { Alert } from '@/shared/components/Alert/Alert';
import { useAddUnit } from '@/features/property-mngt/properties/hooks/useAddUnit';
import { useUnitTypeDefinitions } from '@/features/property-mngt/properties/hooks/useUnitTypeDefinitions';
import { getErrorMessage } from '@/shared/utils/getErrorMessage';
import { addUnitSchema, type AddUnitFormValues } from '@/features/property-mngt/properties/schemas/addUnitSchema';
import { floorLabel } from '@/features/property-mngt/properties/utils/floorLabel';
import { unitNumberFromDigits } from '@/features/property-mngt/properties/utils/unitNumber';
import type { Building } from '@/features/property-mngt/properties/types/property.types';
import { RequiredFieldsHint } from '@/shared/components/RequiredFieldsHint/RequiredFieldsHint';

interface AddUnitFormProps {
  building: Building;
  showShares: boolean;
  onSuccess: () => void;
  onCancel: () => void;
}

export function AddUnitForm({ building, showShares, onSuccess, onCancel }: AddUnitFormProps) {
  const {
    register,
    handleSubmit,
    setValue,
    formState: { errors },
  } = useForm<AddUnitFormValues>({
    resolver: zodResolver(addUnitSchema(building.floorCount)),
    defaultValues: { unitTypeId: '', shares: 0, floor: null },
  });
  const { mutate, isPending, error } = useAddUnit(building.id);
  const unitTypes = useUnitTypeDefinitions(building.propertyId);

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

  // Exactement les étages que l'immeuble déclare (0 = rez-de-chaussée) : proposer
  // une liste bornée plutôt qu'un champ libre rend l'incohérence impossible à
  // saisir, là où un nombre libre ne se serait vu refuser qu'après l'envoi.
  const floorOptions = useMemo(
    () => Array.from({ length: building.floorCount + 1 }, (_, floor) => floor),
    [building.floorCount],
  );

  function onSubmit(values: AddUnitFormValues) {
    // Le champ ne porte que les chiffres ; le lot, lui, s'enregistre préfixé,
    // comme ceux générés à la création de la copropriété - « N° 32 », qui se
    // lira « N° 32 — Appartement » partout où le lot est nommé.
    mutate({ ...values, unitNumber: unitNumberFromDigits(values.unitNumber) }, { onSuccess });
  }

  return (
    <form onSubmit={handleSubmit(onSubmit)} className="flex flex-col gap-4 rounded-lg border border-gray-200 dark:border-gray-800 p-4" noValidate>
      {error && <Alert message={getErrorMessage(error)} />}
      {unitTypes.isError && <Alert message={getErrorMessage(unitTypes.error)} />}
      <RequiredFieldsHint />
      {/* Le type d'abord : c'est lui qui dit ce qu'on ajoute, et le reste de la
          saisie (numéro, étage) se lit différemment pour un appartement et pour
          une place de parking. */}
      <RadioGroup
        label="Type de lot"
        required
        {...register('unitTypeId')}
        errorMessage={errors.unitTypeId?.message}
        options={(unitTypeOptions ?? []).map((unitType) => ({ value: unitType.id, label: unitType.name }))}
        disabled={unitTypes.isLoading}
      />
      {/* Saisie numérique seule, « N° » affiché dans le champ : le syndic tape 32
          et lit « N° 32 », qui est exactement ce qui sera enregistré. inputMode
          ouvre le pavé numérique sur mobile ; le type reste `text` pour éviter
          les flèches et les « e », « + », « - » que type=number laisse passer. */}
      <Input
        label="Numéro de lot"
        required
        prefix="N°"
        inputMode="numeric"
        autoComplete="off"
        placeholder="32"
        {...register('unitNumber')}
        errorMessage={errors.unitNumber?.message}
      />
      <Select
        label="Étage"
        // '' est l'absence de réponse : la valeur d'un <select> est toujours une
        // chaîne, et 0 (le rez-de-chaussée) est un étage parfaitement valable -
        // d'où setValueAs plutôt que valueAsNumber, qui lirait « non renseigné »
        // comme NaN. null/undefined y passent aussi (react-hook-form applique la
        // conversion à la valeur par défaut, et Number(null) vaut 0 : l'étage non
        // renseigné serait devenu le rez-de-chaussée).
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
