import { useQuery } from '@tanstack/react-query';
import { listRecipientCandidates } from '@/features/messaging/api/listRecipientCandidates';
import { queryKeys } from '@/shared/constants/queryKeys';

// True autocomplete, not a browse-everyone list: the query only runs once
// the caller has actually typed something (see RecipientPicker), never for
// an empty/undefined search - matches "autocomplétion, pas l'affichage
// automatique de la liste des utilisateurs".
export function useRecipientCandidates(propertyId: string | undefined, search?: string) {
  return useQuery({
    queryKey: queryKeys.messaging.recipients(propertyId ?? '', search),
    queryFn: () => listRecipientCandidates(propertyId as string, search),
    enabled: Boolean(propertyId) && Boolean(search),
  });
}
