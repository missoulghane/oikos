import { isAxiosError } from 'axios';
import { getApiErrorCode, getErrorMessage } from '@/shared/utils/getErrorMessage';
import { AUTH_ERROR_MESSAGES } from '@/shared/constants/errorMessages';

/**
 * Le message affiché sous le formulaire de connexion.
 *
 * <p>Volontairement plus étroit que getErrorMessage : tout refus de l'API, quelle
 * qu'en soit la raison (identifiant inconnu, mot de passe faux, compte
 * verrouillé, requête malformée), donne la même phrase. Deux formulations
 * différentes suffiraient à trier les adresses qui ont un compte ici de celles
 * qui n'en ont pas.
 *
 * <p>Seul ACCOUNT_NOT_ACTIVATED y échappe : l'API ne le renvoie qu'une fois le mot
 * de passe vérifié (PostPasswordUserDetailsChecker), donc à quelqu'un qui a déjà
 * prouvé qu'il est chez lui.
 */
export function getLoginErrorMessage(error: unknown): string {
  if (getApiErrorCode(error) === 'ACCOUNT_NOT_ACTIVATED') {
    return AUTH_ERROR_MESSAGES.accountNotActivated;
  }

  const status = isAxiosError(error) ? error.response?.status : undefined;
  if (status === 429) {
    return AUTH_ERROR_MESSAGES.tooManyAttempts;
  }
  if (status !== undefined && status >= 400 && status < 500) {
    return AUTH_ERROR_MESSAGES.invalidCredentials;
  }

  // Panne serveur ou réseau : rien à voir avec les identifiants saisis, et
  // répondre « mot de passe invalide » enverrait l'utilisateur en réinitialiser
  // un qui était bon.
  return getErrorMessage(error);
}
