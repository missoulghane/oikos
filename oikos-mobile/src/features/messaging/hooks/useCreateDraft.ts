import { useMutation, useQueryClient } from '@tanstack/react-query';
import { createDraft } from '@/features/messaging/api/createDraft';
import type { SaveMessageDraftPayload } from '@/features/messaging/types/messaging.types';

export function useCreateDraft(propertyId: string) {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: (payload: SaveMessageDraftPayload) => createDraft(propertyId, payload),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['messaging', 'drafts'] });
    },
  });
}
