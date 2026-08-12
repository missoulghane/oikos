import { useEffect, useRef } from 'react';
import * as Device from 'expo-device';
import * as Notifications from 'expo-notifications';
import Constants from 'expo-constants';
import { useRegisterDevicePushToken } from '@/features/notifications/hooks/useRegisterDevicePushToken';

/**
 * Mounted once at the root of MainNavigator (like usePendingInvitationConsumer)
 * so it (re-)registers this device's Expo push token once per app session
 * while authenticated, backing CreateNotificationService's push delivery
 * (see PLAN.md Phase 3). Entirely best-effort: every step here can silently
 * no-op (simulator, permission denied, no EAS project configured yet - see
 * PLAN.md risks) without blocking or surfacing anything to the user, exactly
 * like oikos-web's useCaptureOnboardingLead.
 */
export function usePushTokenRegistration() {
  const registerToken = useRegisterDevicePushToken();
  const hasAttempted = useRef(false);

  useEffect(() => {
    if (hasAttempted.current) {
      return;
    }
    hasAttempted.current = true;

    async function register() {
      // Push tokens don't exist on simulators/emulators - requesting one throws.
      if (!Device.isDevice) {
        return;
      }
      const { status: existingStatus } = await Notifications.getPermissionsAsync();
      const status = existingStatus === 'granted' ? existingStatus : (await Notifications.requestPermissionsAsync()).status;
      if (status !== 'granted') {
        return;
      }
      // No EAS project is configured yet (see PLAN.md Phase 3 risks) - projectId
      // stays undefined until one exists, at which point getExpoPushTokenAsync
      // starts succeeding on its own, with no code change needed here.
      const projectId = Constants.expoConfig?.extra?.eas?.projectId as string | undefined;
      const { data: expoPushToken } = await Notifications.getExpoPushTokenAsync(projectId ? { projectId } : undefined);
      registerToken.mutate(expoPushToken);
    }

    register().catch(() => {
      // Best-effort: no registered device just means no push for this
      // session, never a reason to block or alert the signed-in user.
    });
    // Deliberately runs once per mount (hasAttempted guard above), not on
    // registerToken changing every render (TanStack Query recreates it).
  }, []);
}
