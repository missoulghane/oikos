import { useMutation } from '@tanstack/react-query';
import { registerDevicePushToken } from '@/features/notifications/api/registerDevicePushToken';

export function useRegisterDevicePushToken() {
  return useMutation({
    mutationFn: registerDevicePushToken,
  });
}
