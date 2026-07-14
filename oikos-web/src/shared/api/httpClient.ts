import axios from 'axios';
import { API_URL } from '@/config/env';
import { useAuthStore } from '@/app/store';

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

    if (error.response?.status === 401 && !originalRequest._retry && !isAuthEndpoint) {
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
