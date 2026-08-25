import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import {
  createRecipientGroup,
  deleteRecipientGroup,
  listRecipientGroups,
  updateRecipientGroup,
} from '@/features/messaging/api/recipientGroups';
import { queryKeys } from '@/shared/constants/queryKeys';
import type { SaveRecipientGroupPayload } from '@/features/messaging/types/messaging.types';

export function useRecipientGroups(propertyId: string) {
  return useQuery({
    queryKey: queryKeys.messaging.recipientGroups(propertyId),
    queryFn: () => listRecipientGroups(propertyId),
    enabled: Boolean(propertyId),
  });
}

/** Les trois écritures partagent la même invalidation : l'écran de gestion et le sélecteur lisent la même liste. */
function useGroupMutation<TVariables>(propertyId: string, mutationFn: (variables: TVariables) => Promise<void>) {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn,
    onSuccess: () => queryClient.invalidateQueries({ queryKey: queryKeys.messaging.recipientGroups(propertyId) }),
  });
}

export function useCreateRecipientGroup(propertyId: string) {
  return useGroupMutation<SaveRecipientGroupPayload>(propertyId, (payload) => createRecipientGroup(propertyId, payload));
}

export function useUpdateRecipientGroup(propertyId: string) {
  return useGroupMutation<{ groupId: string; payload: SaveRecipientGroupPayload }>(propertyId, ({ groupId, payload }) =>
    updateRecipientGroup(propertyId, groupId, payload),
  );
}

export function useDeleteRecipientGroup(propertyId: string) {
  return useGroupMutation<string>(propertyId, (groupId) => deleteRecipientGroup(propertyId, groupId));
}
