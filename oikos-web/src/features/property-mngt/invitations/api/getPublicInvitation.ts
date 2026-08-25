import { httpClient } from '@/shared/api/httpClient';
import type { PublicInvitationEnvelope } from '@/features/property-mngt/invitations/types/invitation.types';

/** `invitation` est nul tant que la copropriété n'a pas de lien public : c'est un état, pas une erreur. */
export async function getPublicInvitation(propertyId: string): Promise<PublicInvitationEnvelope> {
  const { data } = await httpClient.get<PublicInvitationEnvelope>(`/properties/${propertyId}/public-invitation`);
  return data;
}
