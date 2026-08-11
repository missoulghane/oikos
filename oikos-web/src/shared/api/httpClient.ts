import axios from 'axios';
import { API_URL } from '@/config/env';
import { useAuthStore } from '@/app/store';

declare module 'axios' {
  interface AxiosRequestConfig {
    /** Voir l'intercepteur de réponse plus bas : requêtes non rattachées à la session. */
    skipSessionRefresh?: boolean;
  }
}

export const httpClient = axios.create({ baseURL: API_URL });

// Dedicated client for the token refresh call: it must never go through the
// interceptors below, or a failing refresh would recursively trigger itself.
const refreshClient = axios.create({ baseURL: API_URL });

httpClient.interceptors.request.use((config) => {
  const { accessToken } = useAuthStore.getState();
  if (accessToken) {
    config.headers.Authorization = `Bearer ${accessToken}`;
  }
  return config;
});

let refreshPromise: Promise<string> | null = null;

async function refreshAccessToken(): Promise<string> {
  const { refreshToken, setSession, clearSession } = useAuthStore.getState();
  if (!refreshToken) {
    throw new Error('No refresh token available');
  }

  try {
    const { data } = await refreshClient.post('/auth/refresh-token', { refreshToken });
    setSession(data);
    return data.accessToken;
  } catch (error) {
    clearSession();
    throw error;
  }
}

httpClient.interceptors.response.use(
  (response) => response,
  async (error) => {
    const originalRequest = error.config;
    const isAuthEndpoint = originalRequest?.url?.startsWith('/auth/');
    // Requests authenticated by something other than the session (the onboarding
    // wizard's own token) opt out: there is no refresh token to renew them with,
    // so the recovery below would bounce an anonymous visitor to /login and drop
    // them out of the funnel. They handle their own 401 instead.
    const skipsSessionRefresh = originalRequest?.skipSessionRefresh === true;

    if (error.response?.status === 401 && !originalRequest._retry && !isAuthEndpoint && !skipsSessionRefresh) {
      originalRequest._retry = true;
      try {
        refreshPromise ??= refreshAccessToken().finally(() => {
          refreshPromise = null;
        });
        const accessToken = await refreshPromise;
        originalRequest.headers.Authorization = `Bearer ${accessToken}`;
        return httpClient(originalRequest);
      } catch (refreshError) {
        window.location.assign('/login');
        return Promise.reject(refreshError);
      }
    }

    return Promise.reject(error);
  },
);
