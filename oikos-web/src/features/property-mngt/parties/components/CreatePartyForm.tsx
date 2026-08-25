import { zodResolver } from '@hookform/resolvers/zod';
import { Controller, useForm, useWatch } from 'react-hook-form';
import { Input } from '@/shared/components/Input/Input';
import { PhoneField } from '@/shared/components/PhoneField/PhoneField';
import { RadioGroup } from '@/shared/components/RadioGroup/RadioGroup';
import { Checkbox } from '@/shared/components/Checkbox/Checkbox';
import { Button } from '@/shared/components/Button/Button';
import { Alert } from '@/shared/components/Alert/Alert';
import { getErrorMessage } from '@/shared/utils/getErrorMessage';
import { useCreateParty } from '@/features/property-mngt/parties/hooks/useCreateParty';
import { createPartySchema, type CreatePartyFormValues } from '@/features/property-mngt/parties/schemas/createPartySchema';
import { PARTY_TYPE_LABELS } from '@/features/property-mngt/properties/constants/partyTypeLabels';
import { PARTY_TYPES } from '@/features/property-mngt/properties/types/property.types';
import { RequiredFieldsHint } from '@/shared/components/RequiredFieldsHint/RequiredFieldsHint';

interface CreatePartyFormProps {
  propertyId: string;
  onSuccess: (partyId: string, fullName: string) => void;
  onCancel?: () => void;
}

/**
 * Creates a standalone contact - not tied to any unit/lot, unlike
 * AddUnitOwnerForm which always creates an ownership alongside the party.
 * Used for contacts that are not copropriétaires (board members, staff...) -
 * suppliers are no longer Party-backed (Partie 2: a supplier is just a
 * charge account/label, see RecordSupplierPaymentForm).
 *
 * Deliberately a <div>, not a <form>: this component can be embedded inline
 * inside other forms - a nested <form> is invalid HTML and made the outer
 * form's submit behave unpredictably. Submission is triggered directly via
 * handleSubmit() on the button's onClick instead of an onSubmit handler.
 */
const PARTY_TYPE_OPTIONS = PARTY_TYPES.map((type) => ({ value: type, label: PARTY_TYPE_LABELS[type] }));

export function CreatePartyForm({ propertyId, onSuccess, onCancel }: CreatePartyFormProps) {
  const {
    register,
    control,
    handleSubmit,
    formState: { errors },
  } = useForm<CreatePartyFormValues>({
    resolver: zodResolver(createPartySchema),
    // Particulier par défaut : c'est le cas de l'écrasante majorité des
    // contacts, et un type non pré-rempli est un champ de plus à chaque saisie.
    defaultValues: { partyType: 'INDIVIDUAL', phone: '', invite: true },
  });
  // useWatch plutôt que watch() : ce dernier renvoie une fonction que le
  // compilateur React ne peut pas mémoriser, et la règle de lint le refuse.
  const invite = useWatch({ control, name: 'invite' });
  const email = useWatch({ control, name: 'email' });
  const { mutate, isPending, error } = useCreateParty(propertyId);

  function onSubmit(values: CreatePartyFormValues) {
    // Une adresse vide part absente : l'API distingue « pas d'email » de « email
    // vide », et c'est la première qui décrit un contact sans adresse.
    mutate(
      { ...values, email: values.email || undefined },
      { onSuccess: (result) => onSuccess(result.id, values.fullName) },
    );
  }

  return (
    <div className="flex flex-col gap-4 rounded-lg border border-gray-200 dark:border-gray-800 p-4">
      {error && <Alert message={getErrorMessage(error)} />}
      <RequiredFieldsHint />
      <Input label="Nom complet" required {...register('fullName')} errorMessage={errors.fullName?.message} />
      <RadioGroup
        label="Type"
        required
        options={PARTY_TYPE_OPTIONS}
        {...register('partyType')}
        errorMessage={errors.partyType?.message}
      />
      <Input label="Email" type="email" {...register('email')} errorMessage={errors.email?.message} />
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
      <Checkbox
        label="Inviter à créer un compte"
        // Sans adresse, l'invitation n'a nulle part où aller.
        disabled={!email}
        hint={
          !email
            ? "Renseignez un email pour pouvoir envoyer une invitation."
            : invite
              ? 'Un lien sera envoyé à cette adresse pour créer un compte.'
              : "Aucun email ne partira : la fiche reste interne au syndic."
        }
        {...register('invite')}
      />
      <div className="flex gap-2">
        <Button type="button" isLoading={isPending} onClick={handleSubmit(onSubmit)}>
          Créer le contact
        </Button>
        {onCancel && (
          <Button type="button" variant="secondary" onClick={onCancel}>
            Annuler
          </Button>
        )}
      </div>
    </div>
  );
}
