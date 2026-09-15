export const API_URL = import.meta.env.VITE_API_URL;

if (!API_URL) {
  throw new Error('VITE_API_URL is not defined. Check your .env file.');
}

/**
 * Adresse citée dans le message d'erreur générique. Pas de garde-fou comme
 * ci-dessus : une valeur par défaut vaut mieux qu'une application qui refuse de
 * démarrer parce qu'on n'a pas encore choisi la boîte du support.
 */
export const SUPPORT_EMAIL = import.meta.env.VITE_SUPPORT_EMAIL || 'support@daba-syndic.com';
