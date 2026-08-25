import { httpClient } from '@/shared/api/httpClient';

/** Rouvre un lien désactivé sur son propre jeton : un lien public ne se recrée jamais, il se réactive. */
export async function enableInvitation(id: string): Promise<void> {
  await httpClient.patch(`/invitations/${id}/enable`);
}
