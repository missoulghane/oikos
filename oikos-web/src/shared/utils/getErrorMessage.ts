import { isAxiosError } from 'axios';
import type { ApiErrorBody } from '@/shared/types/apiError.types';
import { CODE_ERROR_MESSAGES, ERROR_MESSAGES } from '@/shared/constants/errorMessages';

const STATUS_MESSAGES: Record<number, string> = {
  400: ERROR_MESSAGES.invalidRequest,
  401: ERROR_MESSAGES.forbidden,
  403: ERROR_MESSAGES.forbidden,
  404: ERROR_MESSAGES.notFound,
  409: ERROR_MESSAGES.conflict,
  413: ERROR_MESSAGES.payloadTooLarge,
  429: ERROR_MESSAGES.tooManyRequests,
};

/** Le code d'erreur porté par la réponse de l'API, quand elle en porte un. */
export function getApiErrorCode(error: unknown): string | undefined {
  return (isAxiosError<ApiErrorBody>(error) && error.response?.data?.code) || undefined;
}

/**
 * Traduit n'importe quel échec en une phrase française affichable.
 *
 * <p>Ne renvoie jamais le message de l'API ni celui d'une Error : ce sont des
 * textes techniques, en anglais, qui exposent l'intérieur du serveur (noms de
 * champs, identifiants, contraintes de base) sans rien apprendre d'utile à
 * l'utilisateur. Le détail reste visible là où il sert - la réponse HTTP, dans
 * l'onglet réseau, et les logs du serveur.
 */
export function getErrorMessage(error: unknown): string {
  if (!isAxiosError<ApiErrorBody>(error)) {
    return ERROR_MESSAGES.unexpected;
  }

  // Pas de réponse du tout : réseau coupé, serveur injoignable, requête annulée.
  if (!error.response) {
    return ERROR_MESSAGES.network;
  }

  const code = getApiErrorCode(error);
  if (code && CODE_ERROR_MESSAGES[code]) {
    return CODE_ERROR_MESSAGES[code];
  }

  return STATUS_MESSAGES[error.response.status] ?? ERROR_MESSAGES.unexpected;
}
