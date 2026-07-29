import { jwtDecode } from 'jwt-decode';
import { create } from 'zustand';
import { persist } from 'zustand/middleware';
import type { AuthTokens } from '@/features/identity/auth/types/auth.types';

interface AccessTokenClaims {
  sub: string;
  authorities: string[];
}

/**
 * Roles are decoded straight from the JWT rather than fetched separately -
 * they are the global roles baked in at login/refresh (see JwtAuthenticationFilter
 * on the backend). Property-scoped access (which properties a MANAGER manages,
 * which units a USER owns) is deliberately NOT in the token - it is resolved
 * per-request from the database (see useCurrentUser), so a newly granted access
 * is active immediately without requiring the user to log in again.
 */
function decodeRoles(accessToken: string): string[] {
  try {
    return jwtDecode<AccessTokenClaims>(accessToken).authorities ?? [];
  } catch {
    return [];
  }
}

interface AuthState {
  accessToken: string | null;
  refreshToken: string | null;
  isAuthenticated: boolean;
  roles: string[];
  setSession: (tokens: AuthTokens) => void;
  clearSession: () => void;
}

/**
 * Global session store. The connected user's auth state is one of the few
 * things that legitimately belongs in a global store (see naming doc §6):
 * it is read by the router guard, the app layout and the HTTP client alike.
 */
export const useAuthStore = create<AuthState>()(
  persist(
    (set) => ({
      accessToken: null,
      refreshToken: null,
      isAuthenticated: false,
      roles: [],
      setSession: (tokens) =>
        set({
          accessToken: tokens.accessToken,
          refreshToken: tokens.refreshToken,
          isAuthenticated: true,
          roles: decodeRoles(tokens.accessToken),
        }),
      clearSession: () =>
        set({ accessToken: null, refreshToken: null, isAuthenticated: false, roles: [] }),
    }),
    { name: 'oikos-auth' },
  ),
);
