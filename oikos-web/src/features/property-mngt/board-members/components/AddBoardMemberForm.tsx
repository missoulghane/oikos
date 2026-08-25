import { Controller, useForm, useWatch } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { Select } from '@/shared/components/Select/Select';
import { Input } from '@/shared/components/Input/Input';
import { PhoneField } from '@/shared/components/PhoneField/PhoneField';
import { Button } from '@/shared/components/Button/Button';
import { Alert } from '@/shared/components/Alert/Alert';
import { getErrorMessage } from '@/shared/utils/getErrorMessage';
import { useAddBoardMember } from '@/features/property-mngt/board-members/hooks/useAddBoardMember';
import { useBoardMembers } from '@/features/property-mngt/board-members/hooks/useBoardMembers';
import { contactMatchLabel, useContactMatch } from '@/features/property-mngt/parties/hooks/useContactMatch';
import {
  addBoardMemberSchema,
  type AddBoardMemberFormValues,
} from '@/features/property-mngt/board-members/schemas/addBoardMemberSchema';
import { BOARD_ROLES, BOARD_ROLE_LABELS } from '@/features/property-mngt/board-members/types/boardMember.types';
import { RequiredFieldsHint } from '@/shared/components/RequiredFieldsHint/RequiredFieldsHint';

interface AddBoardMemberFormProps {
  propertyId: string;
  onSuccess: () => void;
  onCancel: () => void;
}

/**
 * Le refus « ce contact occupe déjà cette fonction » se voit venir dans la
 * plupart des cas (voir alreadyHoldsThisRole) ; il reste atteignable quand le
 * serveur reconnaît un contact que la recherche de l'écran n'avait pas
 * rapproché. Le message de l'API est alors une phrase technique en anglais :
 * on la remplace par la même explication que celle affichée en amont.
 *
 * <p>Reconnu sur son texte, faute de code d'erreur dans les réponses de l'API
 * (voir ErrorResponse) - même repère court et stable que dans AddUnitOwnerForm.
 */
const ALREADY_HAS_ROLE_API_MESSAGE = 'already holds this management role';

function addBoardMemberErrorMessage(error: unknown): string {
  const message = getErrorMessage(error);
  return message.includes(ALREADY_HAS_ROLE_API_MESSAGE)
    ? "Ce contact occupe déjà cette fonction au conseil. Choisissez-en une autre, ou rattachez un autre contact."
    : message;
}

/**
 * Ajout d'un membre au conseil syndical. Le formulaire donnait l'impression de
 * créer un contact à chaque fois : le trésorier déjà enregistré comme
 * copropriétaire y était ressaisi de zéro, et rien ne disait qu'il existait
 * déjà. Le serveur, lui, rapproche depuis toujours sur l'email puis sur le
 * téléphone (AddBoardMemberService) et réutilise la fiche - l'écran se
 * contentait de ne pas le dire.
 *
 * <p>Même mécanisme et mêmes mots que AddUnitOwnerForm, via le même hook
 * (useContactMatch) : on saisit le contact, et si l'email, le téléphone ou
 * l'adresse de connexion désignent quelqu'un de déjà enregistré, l'écran
 * l'annonce et propose de rattacher cette fiche-là. La confirmation ici est une
 * politesse, pas la garantie : c'est le serveur qui tranche, et deux fiches ne
 * peuvent pas naître d'un double clic.
 */
export function AddBoardMemberForm({ propertyId, onSuccess, onCancel }: AddBoardMemberFormProps) {
  const {
    register,
    control,
    handleSubmit,
    formState: { errors },
  } = useForm<AddBoardMemberFormValues>({
    resolver: zodResolver(addBoardMemberSchema),
    defaultValues: { fullName: '', email: '', phone: '', boardRole: 'MEMBER' },
  });
  const { mutate, isPending, error } = useAddBoardMember(propertyId);
  // Le conseil déjà en place : même clé react-query que la section parente,
  // donc pas d'appel de plus. Sert à ne pas proposer un rattachement que le
  // serveur refusera.
  const boardMembers = useBoardMembers(propertyId);

  // useWatch plutôt que watch() : ce dernier renvoie une fonction que le
  // compilateur React ne peut pas mémoriser, et la règle de lint le refuse.
  const email = useWatch({ control, name: 'email' });
  const phone = useWatch({ control, name: 'phone' });
  const boardRole = useWatch({ control, name: 'boardRole' });

  // Email de fiche, téléphone, puis email de connexion - le même ordre que le
  // serveur (AddBoardMemberService.resolveParty).
  const existingContact = useContactMatch(propertyId, email, phone);

  const existingSeats = existingContact
    ? (boardMembers.data ?? []).filter((member) => member.partyId === existingContact.partyId)
    : [];
  // Le refus porte sur le couple (contact, fonction) : le président peut aussi
  // être trésorier, mais pas deux fois président.
  const alreadyHoldsThisRole = existingSeats.find((member) => member.boardRole === boardRole);
  const otherSeats = existingSeats.filter((member) => member.boardRole !== boardRole);

  function onSubmit(values: AddBoardMemberFormValues) {
    // Coordonnée vide = absente : l'API distingue « pas d'email » de « email
    // vide », et c'est la première qui décrit un membre du conseil sans adresse.
    mutate(
      {
        propertyId,
        fullName: values.fullName,
        email: values.email || undefined,
        phone: values.phone || undefined,
        boardRole: values.boardRole,
      },
      { onSuccess },
    );
  }

  return (
    <form onSubmit={handleSubmit(onSubmit)} className="flex flex-col gap-4" noValidate>
      {error && <Alert message={addBoardMemberErrorMessage(error)} />}
      {/* Deux états, jamais les deux à la fois : « ce contact existe » invite à
          confirmer, « il occupe déjà cette fonction » ferme la porte et dit
          laquelle. */}
      {alreadyHoldsThisRole ? (
        <Alert
          message={`${alreadyHoldsThisRole.partyFullName} occupe déjà la fonction de ${BOARD_ROLE_LABELS[alreadyHoldsThisRole.boardRole]} au conseil. Choisissez-en une autre, ou rattachez un autre contact.`}
        />
      ) : (
        existingContact && (
          <Alert
            variant="warning"
            message={
              `${contactMatchLabel(existingContact)}. En confirmant, la fonction sera rattachée à ce contact — le nom et les coordonnées saisis ici sont ignorés.` +
              (otherSeats.length > 0
                ? ` Il siège déjà au conseil comme ${otherSeats
                    .map((member) => BOARD_ROLE_LABELS[member.boardRole])
                    .join(', ')}.`
                : '')
            }
          />
        )
      )}
      <RequiredFieldsHint />
      <Input label="Nom complet" required {...register('fullName')} errorMessage={errors.fullName?.message} />
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
      <Select label="Rôle" required {...register('boardRole')} errorMessage={errors.boardRole?.message}>
        {BOARD_ROLES.map((role) => (
          <option key={role} value={role}>
            {BOARD_ROLE_LABELS[role]}
          </option>
        ))}
      </Select>

      <div className="flex gap-2">
        {/* Désactivé plutôt que refusé après coup : le message au-dessus dit
            pourquoi, et le syndic n'a pas rempli le reste pour rien. */}
        <Button type="submit" isLoading={isPending} disabled={Boolean(alreadyHoldsThisRole)}>
          {existingContact ? 'Rattacher au contact existant' : 'Ajouter'}
        </Button>
        <Button type="button" variant="secondary" onClick={onCancel}>
          Annuler
        </Button>
      </div>
    </form>
  );
}
