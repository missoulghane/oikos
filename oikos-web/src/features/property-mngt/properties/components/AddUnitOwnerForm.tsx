import { useEffect, useState } from 'react';
import { Controller, useForm, useWatch } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { Input } from '@/shared/components/Input/Input';
import { PhoneField } from '@/shared/components/PhoneField/PhoneField';
import { RadioGroup } from '@/shared/components/RadioGroup/RadioGroup';
import { Checkbox } from '@/shared/components/Checkbox/Checkbox';
import { Button } from '@/shared/components/Button/Button';
import { Alert } from '@/shared/components/Alert/Alert';
import { useAddUnitOwner } from '@/features/property-mngt/properties/hooks/useAddUnitOwner';
import { useParties } from '@/features/property-mngt/parties/hooks/useParties';
import { PARTY_TYPE_LABELS } from '@/features/property-mngt/properties/constants/partyTypeLabels';
import { getErrorMessage } from '@/shared/utils/getErrorMessage';
import { PARTY_TYPES } from '@/features/property-mngt/properties/types/property.types';
import { addUnitOwnerSchema, type AddUnitOwnerFormValues } from '@/features/property-mngt/properties/schemas/addUnitOwnerSchema';

interface AddUnitOwnerFormProps {
  unitId: string;
  propertyId: string;
  onSuccess: () => void;
  onCancel: () => void;
}

const SEARCH_DEBOUNCE_MS = 300;

const PARTY_TYPE_OPTIONS = PARTY_TYPES.map((type) => ({ value: type, label: PARTY_TYPE_LABELS[type] }));

/**
 * Seule porte d'entrée pour rattacher un propriétaire à un lot : on saisit le
 * contact, et si l'email ou le téléphone désignent quelqu'un de déjà enregistré,
 * l'écran le dit et propose de rattacher cette fiche-là plutôt que d'en créer
 * une seconde. L'ancien couple « rattacher un contact existant » / « nouveau
 * contact » obligeait le syndic à savoir, avant de commencer, si la personne
 * était déjà connue - ce que la recherche fait bien mieux que lui.
 *
 * <p>Le serveur applique la même règle (AddUnitOwnerService : email, puis
 * téléphone, puis création) : la confirmation ici est une politesse, pas la
 * garantie. Deux fiches ne peuvent pas naître d'un double clic.
 */
export function AddUnitOwnerForm({ unitId, propertyId, onSuccess, onCancel }: AddUnitOwnerFormProps) {
  const {
    register,
    control,
    handleSubmit,
    formState: { errors },
  } = useForm<AddUnitOwnerFormValues>({
    resolver: zodResolver(addUnitOwnerSchema),
    // Particulier par défaut : c'est l'écrasante majorité des copropriétaires,
    // et un type non pré-rempli est un champ de plus à renseigner à chaque lot.
    defaultValues: { partyType: 'INDIVIDUAL', ownershipShare: 0, phone: '', invite: true },
  });
  const { mutate, isPending, error } = useAddUnitOwner(unitId);

  // useWatch plutôt que watch() : ce dernier renvoie une fonction que le
  // compilateur React ne peut pas mémoriser, et la règle de lint le refuse.
  const email = useWatch({ control, name: 'email' });
  const phone = useWatch({ control, name: 'phone' });
  const invite = useWatch({ control, name: 'invite' });

  const [debouncedSearch, setDebouncedSearch] = useState('');
  useEffect(() => {
    const timeout = setTimeout(() => setDebouncedSearch((email ?? '').trim()), SEARCH_DEBOUNCE_MS);
    return () => clearTimeout(timeout);
  }, [email]);

  const [debouncedPhone, setDebouncedPhone] = useState('');
  useEffect(() => {
    const timeout = setTimeout(() => setDebouncedPhone((phone ?? '').trim()), SEARCH_DEBOUNCE_MS);
    return () => clearTimeout(timeout);
  }, [phone]);

  // Une requête par coordonnée : le serveur cherche sur une chaîne libre, et
  // envoyer les deux d'un coup ne ramènerait que les contacts qui portent
  // exactement les deux.
  const byEmail = useParties(propertyId, 0, { search: debouncedSearch || undefined });
  const byPhone = useParties(propertyId, 0, { search: debouncedPhone || undefined });

  const existingByEmail = debouncedSearch
    ? byEmail.data?.content.find((party) => party.email?.toLowerCase() === debouncedSearch.toLowerCase())
    : undefined;
  const existingByPhone = debouncedPhone
    ? byPhone.data?.content.find((party) => party.phone === debouncedPhone)
    : undefined;
  const existingParty = existingByEmail ?? existingByPhone;
  const matchedOn = existingByEmail ? 'cet email' : 'ce numéro de téléphone';

  function onSubmit(values: AddUnitOwnerFormValues) {
    // Une adresse vide part absente : l'API distingue « pas d'email » de « email
    // vide », et c'est la première qui décrit un copropriétaire sans adresse.
    mutate({ ...values, email: values.email || undefined }, { onSuccess });
  }

  return (
    <form onSubmit={handleSubmit(onSubmit)} className="flex flex-col gap-4 rounded-lg border border-gray-200 dark:border-gray-800 p-4" noValidate>
      {error && <Alert message={getErrorMessage(error)} />}
      {existingParty && (
        <Alert
          variant="warning"
          message={`Un contact existe déjà avec ${matchedOn} : ${existingParty.fullName}. En confirmant, le lot sera rattaché à ce contact — le nom, le type et les coordonnées saisis ici sont ignorés.`}
        />
      )}
      <Input label="Nom complet" {...register('fullName')} errorMessage={errors.fullName?.message} />
      <RadioGroup
        label="Type"
        options={PARTY_TYPE_OPTIONS}
        {...register('partyType')}
        errorMessage={errors.partyType?.message}
      />
      <Input label="Email (optionnel)" type="email" {...register('email')} errorMessage={errors.email?.message} />
      <Controller
        control={control}
        name="phone"
        defaultValue=""
        render={({ field }) => (
          <PhoneField
            label="Téléphone"
            name={field.name}
            value={field.value ?? ''}
            onChange={field.onChange}
            onBlur={field.onBlur}
            errorMessage={errors.phone?.message}
          />
        )}
      />
      <Input
        label="Part de propriété (%)"
        type="number"
        min={0}
        max={100}
        step="any"
        {...register('ownershipShare', { valueAsNumber: true })}
        errorMessage={errors.ownershipShare?.message}
      />
      <Checkbox
        label="Inviter à créer un compte"
        // Sans adresse, l'invitation n'a nulle part où aller : la case est
        // désactivée plutôt que cochée pour rien.
        disabled={!email}
        hint={
          !email
            ? "Renseignez un email pour pouvoir envoyer une invitation."
            : invite
              ? 'Un lien sera envoyé par email pour que ce copropriétaire accède à ses lots.'
              : "Aucun email ne partira : le rattachement reste interne au syndic."
        }
        {...register('invite')}
      />
      <div className="mt-2 flex gap-2">
        <Button type="submit" isLoading={isPending}>
          {existingParty ? 'Rattacher au contact existant' : 'Rattacher au lot'}
        </Button>
        <Button type="button" variant="secondary" onClick={onCancel}>
          Annuler
        </Button>
      </div>
    </form>
  );
}
