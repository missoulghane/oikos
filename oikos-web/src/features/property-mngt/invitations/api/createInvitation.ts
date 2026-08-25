import { httpClient } from '@/shared/api/httpClient';
import type { CreateInvitationPayload } from '@/features/property-mngt/invitations/types/invitation.types';

/**
 * L'API répond 201 avec l'adresse de l'invitation créée. On en extrait l'id
 * plutôt que de le jeter : l'écran qui vient d'envoyer une invitation privée
 * enchaîne en affichant son lien, pour que le syndic puisse aussi le
 * transmettre de la main à la main - par WhatsApp, en séance.
 */
export async function createInvitation({ propertyId, ...body }: CreateInvitationPayload): Promise<{ id: string }> {
  const response = await httpClient.post(`/properties/${propertyId}/invitations`, body);
  const location: string | undefined = response.headers.location;
  return { id: location?.split('/').pop() ?? '' };
}
