import { useMutation, useQueryClient } from '@tanstack/react-query';
import { updateProfile } from '@/features/identity/me/api/updateProfile';
import { queryKeys } from '@/shared/constants/queryKeys';

export function useUpdateProfile() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: updateProfile,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: queryKeys.me.detail() });
    },
  });
}
