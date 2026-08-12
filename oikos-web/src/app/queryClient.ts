import { QueryClient } from '@tanstack/react-query';

/**
 * Module-level singleton, not created inside a component: useAuthStore's
 * clearSession (a plain module, not a hook) needs to reach the same
 * instance AppProviders renders with, so that logging out clears every
 * cached server response along with the tokens - without this, switching
 * accounts in the same tab (logout user2, log in as user7) kept serving
 * user2's cached /users/me, units, installments... until each query's own
 * staleTime happened to expire.
 */
export const queryClient = new QueryClient({
  defaultOptions: {
    queries: {
      retry: 1,
      refetchOnWindowFocus: false,
    },
  },
});
