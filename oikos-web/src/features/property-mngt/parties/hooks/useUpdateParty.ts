import { useMutation, useQueryClient } from '@tanstack/react-query';
import { updateParty } from '@/features/property-mngt/parties/api/updateParty';
import type { UpdatePartyPayload } from '@/features/property-mngt/parties/types/party.types';
import { queryKeys } from '@/shared/constants/queryKeys';

export function useUpdateParty(partyId: string) {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: (payload: UpdatePartyPayload) => updateParty(partyId, payload),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: queryKeys.parties.detail(partyId) });
      // Le nom, l'email et le téléphone s'affichent aussi dans l'annuaire et
      // dans l'onglet Contacts : les laisser en cache, c'est afficher deux
      // vérités selon l'écran d'où l'on vient.
      queryClient.invalidateQueries({ queryKey: ['parties', 'list'] });
      queryClient.invalidateQueries({ queryKey: ['properties'] });
    },
  });
}
