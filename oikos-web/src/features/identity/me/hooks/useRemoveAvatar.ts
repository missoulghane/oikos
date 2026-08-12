import { useMutation, useQueryClient } from '@tanstack/react-query';
import { removeAvatar } from '@/features/identity/me/api/removeAvatar';
import { queryKeys } from '@/shared/constants/queryKeys';

export function useRemoveAvatar() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: removeAvatar,
    onSuccess: (user) => {
      queryClient.setQueryData(queryKeys.me.detail(), user);
      queryClient.invalidateQueries({ queryKey: ['me', 'avatar'] });
    },
  });
}
