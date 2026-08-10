import { useQuery } from '@tanstack/react-query';
import { getDraft } from '@/features/messaging/api/getDraft';
import { queryKeys } from '@/shared/constants/queryKeys';

export function useDraft(draftId: string | undefined) {
  return useQuery({
    queryKey: queryKeys.messaging.draft(draftId ?? ''),
    queryFn: () => getDraft(draftId as string),
    enabled: Boolean(draftId),
  });
}
