import { useMutation } from '@tanstack/react-query';
import { login } from '@/features/auth/api/login';
import { useAuthStore } from '@/app/store';
import type { LoginCredentials } from '@/features/auth/types/auth.types';

export function useLogin() {
  const setSession = useAuthStore((state) => state.setSession);

  return useMutation({
    mutationFn: (credentials: LoginCredentials) => login(credentials),
    onSuccess: setSession,
  });
}
