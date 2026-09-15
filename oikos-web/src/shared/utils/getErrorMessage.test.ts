import { describe, expect, it } from 'vitest';
import { AxiosError, AxiosHeaders } from 'axios';
import { getApiErrorCode, getErrorMessage } from '@/shared/utils/getErrorMessage';
import { ERROR_MESSAGES } from '@/shared/constants/errorMessages';
import type { ApiErrorBody } from '@/shared/types/apiError.types';

function apiError(status: number, message: string, code?: string): AxiosError<ApiErrorBody> {
  const error = new AxiosError<ApiErrorBody>('Request failed', String(status));
  error.response = {
    data: { status, error: 'Error', message, code, path: '/properties', timestamp: '' },
    status,
    statusText: 'Error',
    headers: {},
    config: { headers: new AxiosHeaders() },
  };
  return error;
}

describe('getErrorMessage', () => {
  it('ne laisse jamais fuiter le message technique de l’API', () => {
    const message = getErrorMessage(apiError(400, 'name: must not be blank'));

    expect(message).toBe(ERROR_MESSAGES.invalidRequest);
    expect(message).not.toContain('must not be blank');
  });

  it('renvoie un message générique avec le support pour une panne serveur', () => {
    const message = getErrorMessage(apiError(500, 'An unexpected error occurred'));

    expect(message).toBe(ERROR_MESSAGES.unexpected);
    expect(message).toMatch(/support/i);
  });

  it('distingue le serveur injoignable, qui n’a rien d’une erreur métier', () => {
    expect(getErrorMessage(new AxiosError('Network Error'))).toBe(ERROR_MESSAGES.network);
  });

  it('choisit sa phrase sur le statut HTTP', () => {
    expect(getErrorMessage(apiError(403, 'Access is denied'))).toBe(ERROR_MESSAGES.forbidden);
    expect(getErrorMessage(apiError(404, 'No property found with id: 42'))).toBe(ERROR_MESSAGES.notFound);
    expect(getErrorMessage(apiError(409, 'A party already exists with email: a@b.c'))).toBe(
      ERROR_MESSAGES.conflict,
    );
    expect(getErrorMessage(apiError(429, 'Too many requests'))).toBe(ERROR_MESSAGES.tooManyRequests);
  });

  it('préfère le code d’erreur au statut quand l’API en donne un', () => {
    expect(getErrorMessage(apiError(400, 'Verification token has expired', 'INVALID_LINK'))).toBe(
      ERROR_MESSAGES.invalidLink,
    );
  });

  it('ignore un code inconnu plutôt que de l’afficher', () => {
    expect(getErrorMessage(apiError(404, 'nope', 'SOME_FUTURE_CODE'))).toBe(ERROR_MESSAGES.notFound);
  });

  it('retombe sur le message générique pour tout ce qui n’est pas une réponse HTTP', () => {
    expect(getErrorMessage(new Error('boom'))).toBe(ERROR_MESSAGES.unexpected);
    expect(getErrorMessage('not an error')).toBe(ERROR_MESSAGES.unexpected);
  });
});

describe('getApiErrorCode', () => {
  it('remonte le code de la réponse, et rien quand il n’y en a pas', () => {
    expect(getApiErrorCode(apiError(403, 'Invalid credentials', 'INVALID_CREDENTIALS'))).toBe(
      'INVALID_CREDENTIALS',
    );
    expect(getApiErrorCode(apiError(403, 'Invalid credentials'))).toBeUndefined();
    expect(getApiErrorCode(new Error('boom'))).toBeUndefined();
  });
});
