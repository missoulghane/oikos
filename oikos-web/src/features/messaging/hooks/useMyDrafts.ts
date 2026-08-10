import { useQuery } from '@tanstack/react-query';
import { listMyDrafts } from '@/features/messaging/api/listMyDrafts';
import { queryKeys } from '@/shared/constants/queryKeys';

const DEFAULT_PAGE_SIZE = 20;

export function useMyDrafts(page: number, search?: string, size: number = DEFAULT_PAGE_SIZE) {
  return useQuery({
    queryKey: queryKeys.messaging.drafts(page, size, search),
    queryFn: () => listMyDrafts({ page, size, search }),
    placeholderData: (previousData) => previousData,
  });
}
