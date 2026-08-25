import { useEffect, useState } from 'react';
import { useQuery } from '@tanstack/react-query';
import { useParties } from '@/features/property-mngt/parties/hooks/useParties';
import { getContactByAccountEmail } from '@/features/property-mngt/properties/api/getContactByAccountEmail';
import { queryKeys } from '@/shared/constants/queryKeys';

const SEARCH_DEBOUNCE_MS = 300;

export type ContactMatchedOn = 'EMAIL' | 'PHONE' | 'ACCOUNT_EMAIL';

export interface ContactMatch {
  partyId: string;
  fullName: string;
  /** L'adresse portée par la fiche, qui n'est pas celle cherchée quand matchedOn vaut ACCOUNT_EMAIL. */
  contactEmail: string | null;
  matchedOn: ContactMatchedOn;
}

/**
 * « Cette personne est-elle déjà au fichier de la copropriété ? », posé au fil
 * de la frappe par les écrans qui rattachent quelqu'un (un lot, un siège au
 * conseil).
 *
 * <p>Trois filets, dans l'ordre exact où le serveur les pose
 * (AddUnitOwnerService / AddBoardMemberService) - sans quoi l'écran
 * annoncerait une fiche et le serveur en retiendrait une autre :
 *
 * <ol>
 *   <li>l'email de la fiche contact,</li>
 *   <li>le téléphone de la fiche contact,</li>
 *   <li>l'email de connexion du compte, qui n'est pas toujours celui de la
 *       fiche - c'est le cas le plus trompeur, et celui qui fabriquait des
 *       doublons en silence.</li>
 * </ol>
 *
 * <p>Un hook partagé plutôt que le même code recopié dans chaque formulaire :
 * les deux écrans doivent rapprocher à l'identique, et c'est en les laissant
 * diverger que l'un a fini par ne rien rapprocher du tout.
 */
export function useContactMatch(propertyId: string, email: string | undefined, phone: string | undefined) {
  const [debouncedEmail, setDebouncedEmail] = useState('');
  useEffect(() => {
    const timeout = setTimeout(() => setDebouncedEmail((email ?? '').trim()), SEARCH_DEBOUNCE_MS);
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
  const byEmail = useParties(propertyId, 0, { search: debouncedEmail || undefined });
  const byPhone = useParties(propertyId, 0, { search: debouncedPhone || undefined });

  // Inutile d'interroger les comptes sur une adresse en cours de frappe : le
  // serveur répondrait « personne » de toute façon.
  const looksLikeEmail = /.+@.+\..+/.test(debouncedEmail);
  const byAccountEmail = useQuery({
    queryKey: queryKeys.parties.byAccountEmail(propertyId, debouncedEmail),
    queryFn: () => getContactByAccountEmail(propertyId, debouncedEmail),
    enabled: looksLikeEmail,
  });

  const matchedByEmail = debouncedEmail
    ? byEmail.data?.content.find((party) => party.email?.toLowerCase() === debouncedEmail.toLowerCase())
    : undefined;
  const matchedByPhone = debouncedPhone
    ? byPhone.data?.content.find((party) => party.phone === debouncedPhone)
    : undefined;
  const matchedByAccount = looksLikeEmail ? (byAccountEmail.data ?? undefined) : undefined;

  if (matchedByEmail) {
    return {
      partyId: matchedByEmail.id,
      fullName: matchedByEmail.fullName,
      contactEmail: matchedByEmail.email ?? null,
      matchedOn: 'EMAIL',
    } satisfies ContactMatch;
  }
  if (matchedByPhone) {
    return {
      partyId: matchedByPhone.id,
      fullName: matchedByPhone.fullName,
      contactEmail: matchedByPhone.email ?? null,
      matchedOn: 'PHONE',
    } satisfies ContactMatch;
  }
  if (matchedByAccount) {
    return {
      partyId: matchedByAccount.partyId,
      fullName: matchedByAccount.fullName,
      contactEmail: matchedByAccount.email,
      matchedOn: 'ACCOUNT_EMAIL',
    } satisfies ContactMatch;
  }
  return undefined;
}

/** La phrase d'accroche du rapprochement, selon ce qui a permis de reconnaître la personne. */
export function contactMatchLabel(match: ContactMatch): string {
  if (match.matchedOn === 'ACCOUNT_EMAIL') {
    return `${match.fullName} se connecte déjà avec cette adresse. Sa fiche dans cette copropriété est au nom de ${match.fullName}${
      match.contactEmail ? ` (${match.contactEmail})` : ''
    }`;
  }
  const coordinate = match.matchedOn === 'EMAIL' ? 'cet email' : 'ce numéro de téléphone';
  return `Un contact existe déjà avec ${coordinate} : ${match.fullName}`;
}
