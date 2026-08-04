import { useMutation, useQueryClient } from '@tanstack/react-query';
import { updatePartyPhone } from '@/features/property-mngt/parties/api/updatePartyPhone';
import { queryKeys } from '@/shared/constants/queryKeys';

export function useUpdatePartyPhone(partyId: string) {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: (phone: string | undefined) => updatePartyPhone(partyId, phone),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: queryKeys.parties.detail(partyId) });
    },
  });
}
