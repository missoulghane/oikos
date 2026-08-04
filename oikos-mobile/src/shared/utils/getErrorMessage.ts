import { isAxiosError } from 'axios';
import type { ApiErrorBody } from '@/shared/types/apiError.types';

const FALLBACK_MESSAGE = 'Une erreur inattendue est survenue. Merci de réessayer.';

export function getErrorMessage(error: unknown): string {
  if (isAxiosError<ApiErrorBody>(error)) {
    return error.response?.data?.message ?? error.message ?? FALLBACK_MESSAGE;
  }

  if (error instanceof Error) {
    return error.message;
  }

  return FALLBACK_MESSAGE;
}
