import { QueryClient } from '@tanstack/react-query';

/**
 * Module-level singleton, not created inside a component: useAuthStore's
 * clearSession (a plain module, not a hook) needs to reach the same
 * instance AppProviders renders with, so that logging out clears every
 * cached server response along with the tokens - without this, switching
 * accounts on the same device kept serving the previous account's cached
 * /users/me, units, installments... until each query's own staleTime
 * happened to expire. Mirrors app/queryClient.ts in oikos-web.
 */
export const queryClient = new QueryClient({
  defaultOptions: {
    queries: {
      staleTime: 60_000,
      retry: 1,
    },
  },
});
