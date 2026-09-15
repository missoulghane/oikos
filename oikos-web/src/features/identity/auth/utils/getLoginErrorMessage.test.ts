import { describe, expect, it } from 'vitest';
import { AxiosError, AxiosHeaders } from 'axios';
import { getLoginErrorMessage } from '@/features/identity/auth/utils/getLoginErrorMessage';
import { AUTH_ERROR_MESSAGES, ERROR_MESSAGES } from '@/shared/constants/errorMessages';
import type { ApiErrorBody } from '@/shared/types/apiError.types';

function apiError(status: number, message: string, code?: string): AxiosError<ApiErrorBody> {
  const error = new AxiosError<ApiErrorBody>('Request failed', String(status));
  error.response = {
    data: { status, error: 'Error', message, code, path: '/auth/login', timestamp: '' },
    status,
    statusText: 'Error',
    headers: {},
    config: { headers: new AxiosHeaders() },
  };
  return error;
}

describe('getLoginErrorMessage', () => {
  it('répond la même phrase à un identifiant inconnu et à un mot de passe faux', () => {
    const unknownIdentifier = getLoginErrorMessage(
      apiError(403, 'Invalid credentials', 'INVALID_CREDENTIALS'),
    );
    const wrongPassword = getLoginErrorMessage(apiError(403, 'Invalid credentials', 'INVALID_CREDENTIALS'));

    expect(unknownIdentifier).toBe(AUTH_ERROR_MESSAGES.invalidCredentials);
    expect(unknownIdentifier).toBe(wrongPassword);
    expect(unknownIdentifier).toBe('Identifiant ou mot de passe invalide.');
  });

  it('ne dit rien de plus quand l’API refuse sans code', () => {
    expect(getLoginErrorMessage(apiError(401, 'Bad credentials'))).toBe(
      AUTH_ERROR_MESSAGES.invalidCredentials,
    );
    expect(getLoginErrorMessage(apiError(400, 'identifier: must not be blank'))).toBe(
      AUTH_ERROR_MESSAGES.invalidCredentials,
    );
  });

  it('dit le compte non activé, que l’API ne renvoie qu’après un mot de passe correct', () => {
    expect(getLoginErrorMessage(apiError(403, 'Account is not activated', 'ACCOUNT_NOT_ACTIVATED'))).toBe(
      AUTH_ERROR_MESSAGES.accountNotActivated,
    );
  });

  it('annonce le plafond de tentatives plutôt qu’un mot de passe faux', () => {
    expect(getLoginErrorMessage(apiError(429, 'Too many requests'))).toBe(
      AUTH_ERROR_MESSAGES.tooManyAttempts,
    );
  });

  it('n’accuse pas les identifiants quand la panne vient du serveur ou du réseau', () => {
    expect(getLoginErrorMessage(apiError(500, 'An unexpected error occurred'))).toBe(
      ERROR_MESSAGES.unexpected,
    );
    expect(getLoginErrorMessage(new AxiosError('Network Error'))).toBe(ERROR_MESSAGES.network);
  });
});
