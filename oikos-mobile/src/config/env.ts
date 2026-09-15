export const API_URL = process.env.EXPO_PUBLIC_API_URL;

if (!API_URL) {
  throw new Error('EXPO_PUBLIC_API_URL is not defined. Check your .env file.');
}

/**
 * Adresse citée dans le message d'erreur générique. Pas de garde-fou comme
 * ci-dessus : une valeur par défaut vaut mieux qu'une application qui refuse de
 * démarrer parce qu'on n'a pas encore choisi la boîte du support.
 */
export const SUPPORT_EMAIL = process.env.EXPO_PUBLIC_SUPPORT_EMAIL || 'support@daba-syndic.com';
