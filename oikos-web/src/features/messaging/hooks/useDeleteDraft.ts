import { useMutation, useQueryClient } from '@tanstack/react-query';
import { deleteDraft } from '@/features/messaging/api/deleteDraft';

export function useDeleteDraft() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: (draftId: string) => deleteDraft(draftId),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['messaging', 'drafts'] });
    },
  });
}
