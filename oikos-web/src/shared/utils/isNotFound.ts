import { isAxiosError } from 'axios';

/** True for a 404, so a caller can say "this does not exist" rather than echo a raw server message. */
export function isNotFound(error: unknown): boolean {
  return isAxiosError(error) && error.response?.status === 404;
}
