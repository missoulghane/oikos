import { useMutation, useQueryClient } from '@tanstack/react-query';
import { updateDraft } from '@/features/messaging/api/updateDraft';
import type { SaveMessageDraftPayload } from '@/features/messaging/types/messaging.types';

export function useUpdateDraft(draftId: string) {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: (payload: SaveMessageDraftPayload) => updateDraft(draftId, payload),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['messaging', 'drafts'] });
    },
  });
}
