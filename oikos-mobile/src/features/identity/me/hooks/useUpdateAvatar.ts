import { useMutation, useQueryClient } from '@tanstack/react-query';
import { updateAvatar } from '@/features/identity/me/api/updateAvatar';
import { queryKeys } from '@/shared/constants/queryKeys';

export function useUpdateAvatar() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: updateAvatar,
    onSuccess: (user) => {
      queryClient.setQueryData(queryKeys.me.detail(), user);
      queryClient.invalidateQueries({ queryKey: ['me', 'avatar'] });
    },
  });
}
