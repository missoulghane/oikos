import { jwtDecode } from 'jwt-decode';
import { Platform } from 'react-native';
import { create } from 'zustand';
import { createJSONStorage, persist, type StateStorage } from 'zustand/middleware';
import * as SecureStore from 'expo-secure-store';
import type { AuthTokens } from '@/features/identity/auth/types/auth.types';

interface AccessTokenClaims {
  sub: string;
  authorities: string[];
}

/**
 * Roles are decoded straight from the JWT rather than fetched separately - they
 * are the global roles baked in at login/refresh (see JwtAuthenticationFilter on
 * the backend). Property-scoped access is deliberately NOT in the token - it is
 * resolved per-request from the database (see useCurrentUser), so a newly
 * granted access is active immediately without requiring the user to log in
 * again. Mirrors app/store.ts in oikos-web.
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
  hasHydrated: boolean;
  setSession: (tokens: AuthTokens) => void;
  clearSession: () => void;
}

// expo-secure-store wraps Keychain (iOS) / Keystore (Android); neither exists
// on web, so the web build falls back to localStorage - the same mechanism
// oikos-web itself relies on (see its app/store.ts persist call).
const authStorage: StateStorage =
  Platform.OS === 'web'
    ? {
        getItem: (name) => Promise.resolve(globalThis.localStorage?.getItem(name) ?? null),
        setItem: (name, value) => {
          globalThis.localStorage?.setItem(name, value);
          return Promise.resolve();
        },
        removeItem: (name) => {
          globalThis.localStorage?.removeItem(name);
          return Promise.resolve();
        },
      }
    : {
        getItem: (name) => SecureStore.getItemAsync(name),
        setItem: (name, value) => SecureStore.setItemAsync(name, value),
        removeItem: (name) => SecureStore.deleteItemAsync(name),
      };

/**
 * Global session store. The connected user's auth state is one of the few
 * things that legitimately belongs in a global store: it is read by the root
 * navigator, the app layout and the HTTP client alike. Tokens are persisted in
 * SecureStore (Keychain/Keystore-backed) on native, never AsyncStorage.
 */
export const useAuthStore = create<AuthState>()(
  persist(
    (set) => ({
      accessToken: null,
      refreshToken: null,
      isAuthenticated: false,
      roles: [],
      hasHydrated: false,
      setSession: (tokens) =>
        set({
          accessToken: tokens.accessToken,
          refreshToken: tokens.refreshToken,
          isAuthenticated: true,
          roles: decodeRoles(tokens.accessToken),
        }),
      clearSession: () => set({ accessToken: null, refreshToken: null, isAuthenticated: false, roles: [] }),
    }),
    {
      name: 'oikos-auth',
      storage: createJSONStorage(() => authStorage),
      onRehydrateStorage: () => () => useAuthStore.setState({ hasHydrated: true }),
    },
  ),
);
