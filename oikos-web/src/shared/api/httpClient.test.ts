import { describe, expect, it, beforeEach } from 'vitest';
import { httpClient } from '@/shared/api/httpClient';
import { useAuthStore } from '@/app/store';

/**
 * L'intercepteur de requête est exécuté seul (sans appel réseau) : ce qui compte
 * ici est uniquement l'en-tête qu'il laisse en place.
 */
async function runRequestInterceptors(config: Parameters<typeof httpClient.request>[0]) {
  // La chaîne d'intercepteurs n'est pas exposée par le typage public d'axios.
  const handlers = (httpClient.interceptors.request as unknown as {
    handlers: { fulfilled: (config: unknown) => unknown }[];
  }).handlers;
  return handlers.reduce<unknown>((current, handler) => handler.fulfilled(current), {
    ...config,
    headers: { ...(config?.headers ?? {}) },
  }) as { headers: Record<string, string> };
}

describe('httpClient request interceptor', () => {
  beforeEach(() => {
    useAuthStore.getState().clearSession();
  });

  it('pose le jeton de session quand la requête n en porte pas', async () => {
    useAuthStore.setState({ accessToken: 'session-token' });

    const result = await runRequestInterceptors({ url: '/users/me' });

    expect(result.headers.Authorization).toBe('Bearer session-token');
  });

  it('laisse intact un Authorization posé par l appelant', async () => {
    // Le cas du wizard d'inscription : son jeton d'onboarding est le seul valable
    // sur /properties/{id}/configuration, et une session (même expirée) traîne
    // souvent dans le localStorage du même navigateur.
    useAuthStore.setState({ accessToken: 'session-token' });

    const result = await runRequestInterceptors({
      url: '/properties/abc/configuration',
      headers: { Authorization: 'Bearer onboarding-token' },
    });

    expect(result.headers.Authorization).toBe('Bearer onboarding-token');
  });
});
