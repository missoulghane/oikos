import { create } from 'zustand';
import { persist } from 'zustand/middleware';
import type { AuthTokens } from '@/features/identity/auth/types/auth.types';

interface AuthState {
  accessToken: string | null;
  refreshToken: string | null;
  isAuthenticated: boolean;
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
      setSession: (tokens) =>
        set({
          accessToken: tokens.accessToken,
          refreshToken: tokens.refreshToken,
          isAuthenticated: true,
        }),
      clearSession: () =>
        set({ accessToken: null, refreshToken: null, isAuthenticated: false }),
    }),
    { name: 'oikos-auth' },
  ),
);
