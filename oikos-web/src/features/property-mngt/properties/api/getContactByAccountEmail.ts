import { httpClient } from '@/shared/api/httpClient';

/** Mirrors LinkedContactResponse (oikos-api) - null quand personne ne se connecte avec cette adresse. */
export interface LinkedContact {
  partyId: string;
  fullName: string;
  /** L'adresse de la fiche, souvent différente de celle qu'on a cherchée. */
  email: string | null;
}

export async function getContactByAccountEmail(propertyId: string, email: string): Promise<LinkedContact | null> {
  const { data } = await httpClient.get<LinkedContact | ''>(`/properties/${propertyId}/contacts/by-account-email`, {
    params: { email },
  });
  // Un corps vide (204-like) arrive comme chaîne vide chez axios : c'est le
  // « personne ne correspond » du serveur, pas une réponse mal formée.
  return data === '' ? null : data;
}
