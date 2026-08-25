import { Controller, useForm, useWatch } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { Input } from '@/shared/components/Input/Input';
import { PhoneField } from '@/shared/components/PhoneField/PhoneField';
import { RadioGroup } from '@/shared/components/RadioGroup/RadioGroup';
import { Checkbox } from '@/shared/components/Checkbox/Checkbox';
import { Button } from '@/shared/components/Button/Button';
import { Alert } from '@/shared/components/Alert/Alert';
import { useAddUnitOwner } from '@/features/property-mngt/properties/hooks/useAddUnitOwner';
import { useUnitOwners } from '@/features/property-mngt/properties/hooks/useUnitOwners';
import { contactMatchLabel, useContactMatch } from '@/features/property-mngt/parties/hooks/useContactMatch';
import { PARTY_TYPE_LABELS } from '@/features/property-mngt/properties/constants/partyTypeLabels';
import { getErrorMessage } from '@/shared/utils/getErrorMessage';
import { PARTY_TYPES } from '@/features/property-mngt/properties/types/property.types';
import { addUnitOwnerSchema, type AddUnitOwnerFormValues } from '@/features/property-mngt/properties/schemas/addUnitOwnerSchema';
import { RequiredFieldsHint } from '@/shared/components/RequiredFieldsHint/RequiredFieldsHint';

interface AddUnitOwnerFormProps {
  unitId: string;
  propertyId: string;
  onSuccess: () => void;
  onCancel: () => void;
}

const PARTY_TYPE_OPTIONS = PARTY_TYPES.map((type) => ({ value: type, label: PARTY_TYPE_LABELS[type] }));

/**
 * Le refus « ce contact détient déjà une part sur ce lot » se voit venir dans
 * la plupart des cas (voir alreadyOwnerHere) ; il reste atteignable quand le
 * serveur reconnaît un contact que la recherche de l'écran n'avait pas
 * rapproché - une adresse saisie ici qui, côté serveur, désigne une fiche déjà
 * rattachée. Le message de l'API est alors une phrase technique en anglais :
 * on la remplace par la même explication que celle affichée en amont.
 *
 * <p>Reconnu sur son texte, faute de code d'erreur dans les réponses de l'API
 * (voir ErrorResponse) : le repère est volontairement court et stable, et
 * l'appel retombe sur le message d'origine s'il ne correspond pas.
 */
const ALREADY_OWNER_API_MESSAGE = 'already registered as an owner';

function addUnitOwnerErrorMessage(error: unknown): string {
  const message = getErrorMessage(error);
  return message.includes(ALREADY_OWNER_API_MESSAGE)
    ? "Ce contact détient déjà une part de ce lot. Un même contact n'y figure qu'une fois : vérifiez la liste des propriétaires ci-dessous."
    : message;
}

/**
 * Seule porte d'entrée pour rattacher un propriétaire à un lot : on saisit le
 * contact, et si l'email, le téléphone ou l'adresse de connexion désignent
 * quelqu'un de déjà enregistré, l'écran le dit et propose de rattacher cette
 * fiche-là plutôt que d'en créer une seconde. L'ancien couple « rattacher un contact existant » / « nouveau
 * contact » obligeait le syndic à savoir, avant de commencer, si la personne
 * était déjà connue - ce que la recherche fait bien mieux que lui.
 *
 * <p>Le serveur applique la même règle, dans le même ordre (AddUnitOwnerService :
 * email de fiche, téléphone, email de compte, puis création) - c'est le même
 * hook qui l'imite ici et dans AddBoardMemberForm (useContactMatch). La
 * confirmation ici est une politesse, pas la garantie. Deux fiches ne peuvent
 * pas naître d'un double clic.
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
  // Les propriétaires déjà rattachés à ce lot : la requête est celle que la
  // section parente affiche déjà (même clé react-query, donc pas d'appel de
  // plus), et elle sert ici à ne pas proposer un rattachement que le serveur
  // refusera.
  const unitOwners = useUnitOwners(unitId);

  // useWatch plutôt que watch() : ce dernier renvoie une fonction que le
  // compilateur React ne peut pas mémoriser, et la règle de lint le refuse.
  const email = useWatch({ control, name: 'email' });
  const phone = useWatch({ control, name: 'phone' });
  const invite = useWatch({ control, name: 'invite' });

  // Email de fiche, téléphone, puis email de connexion - le même ordre que le
  // serveur (AddUnitOwnerService).
  const existingParty = useContactMatch(propertyId, email, phone);

  // Un contact ne peut détenir qu'une seule quote-part sur un même lot (RG côté
  // API : PartyAlreadyOwnsUnitException). Le dire ici, avant l'envoi, plutôt que
  // de laisser le serveur répondre en anglais technique une fois le formulaire
  // rempli - c'est le seul refus que le formulaire peut voir venir, puisque la
  // liste des propriétaires du lot est déjà chargée.
  const alreadyOwnerHere = existingParty
    ? unitOwners.data?.find((owner) => owner.partyId === existingParty.partyId)
    : undefined;

  function onSubmit(values: AddUnitOwnerFormValues) {
    // Une adresse vide part absente : l'API distingue « pas d'email » de « email
    // vide », et c'est la première qui décrit un copropriétaire sans adresse.
    mutate({ ...values, email: values.email || undefined }, { onSuccess });
  }

  return (
    <form onSubmit={handleSubmit(onSubmit)} className="flex flex-col gap-4 rounded-lg border border-gray-200 dark:border-gray-800 p-4" noValidate>
      {error && <Alert message={addUnitOwnerErrorMessage(error)} />}
      {/* Deux états, jamais les deux à la fois : « ce contact existe » invite à
          confirmer, « il détient déjà une part ici » ferme la porte et dit
          laquelle. */}
      {alreadyOwnerHere ? (
        <Alert
          message={`${alreadyOwnerHere.partyFullName} détient déjà ${alreadyOwnerHere.ownershipShare} % de ce lot. Un même contact n'y figure qu'une fois : vérifiez la liste des propriétaires ci-dessous, ou rattachez un autre contact.`}
        />
      ) : (
        existingParty && (
          <Alert
            variant="warning"
            message={`${contactMatchLabel(existingParty)}. En confirmant, le lot sera rattaché à ce contact — le nom, le type et les coordonnées saisis ici sont ignorés.`}
          />
        )
      )}
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
      <Input
        label="Part de propriété (%)"
        required
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
        {/* Désactivé plutôt que refusé après coup : le message au-dessus dit
            pourquoi, et le syndic n'a pas rempli le reste pour rien. */}
        <Button type="submit" isLoading={isPending} disabled={Boolean(alreadyOwnerHere)}>
          {existingParty ? 'Rattacher au contact existant' : 'Rattacher au lot'}
        </Button>
        <Button type="button" variant="secondary" onClick={onCancel}>
          Annuler
        </Button>
      </div>
    </form>
  );
}
