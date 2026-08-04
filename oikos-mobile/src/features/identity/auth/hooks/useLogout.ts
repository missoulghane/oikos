import { useAuthStore } from '@/app/store';

/**
 * Logout is purely local (no /auth/logout endpoint on the backend, mirrors
 * oikos-web's UserDropdown): clearing the session flips isAuthenticated to
 * false, which the root navigator reacts to by switching back to Auth.
 */
export function useLogout() {
  const clearSession = useAuthStore((state) => state.clearSession);
  return clearSession;
}
