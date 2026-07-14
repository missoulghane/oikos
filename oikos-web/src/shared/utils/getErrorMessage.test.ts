import { describe, expect, it } from 'vitest';
import { AxiosError } from 'axios';
import { getErrorMessage } from '@/shared/utils/getErrorMessage';

describe('getErrorMessage', () => {
  it('extracts the API message from an Axios error response', () => {
    const error = new AxiosError('Request failed', '400');
    error.response = {
      data: { status: 400, error: 'Bad Request', message: 'name: must not be blank', path: '/properties', timestamp: '' },
      status: 400,
      statusText: 'Bad Request',
      headers: {},
      // @ts-expect-error partial config is enough for this test
      config: {},
    };

    expect(getErrorMessage(error)).toBe('name: must not be blank');
  });

  it('falls back to the generic Error message', () => {
    expect(getErrorMessage(new Error('boom'))).toBe('boom');
  });

  it('falls back to a generic message for unknown values', () => {
    expect(getErrorMessage('not an error')).toBe('Une erreur inattendue est survenue. Merci de réessayer.');
  });
});
