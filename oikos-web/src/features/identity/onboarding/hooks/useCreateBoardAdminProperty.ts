import { useMutation } from '@tanstack/react-query';
import { createBoardAdminProperty } from '@/features/identity/onboarding/api/createBoardAdminProperty';

export function useCreateBoardAdminProperty() {
  return useMutation({ mutationFn: createBoardAdminProperty });
}
