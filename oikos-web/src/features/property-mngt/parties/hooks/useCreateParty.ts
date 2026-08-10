import { useMutation, useQueryClient } from '@tanstack/react-query';
import { createParty } from '@/features/property-mngt/parties/api/createParty';
import type { CreatePartyPayload } from '@/features/property-mngt/parties/types/party.types';

export function useCreateParty(propertyId: string) {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: (payload: Omit<CreatePartyPayload, 'propertyId'>) => createParty({ ...payload, propertyId }),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['parties', 'list', propertyId] });
    },
  });
}
