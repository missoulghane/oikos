import { httpClient } from '@/shared/api/httpClient';

export async function registerDevicePushToken(expoPushToken: string): Promise<void> {
  await httpClient.post('/users/me/push-tokens', { expoPushToken });
}
