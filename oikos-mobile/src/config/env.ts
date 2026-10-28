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

/**
 * Site web où vivent les pages publiques que l'application ouvre dans le
 * navigateur - CGU et CGV à l'inscription. Même logique que le support : un
 * défaut plutôt qu'un refus de démarrer, le domaine public servant ces pages.
 */
export const WEB_URL = process.env.EXPO_PUBLIC_WEB_URL || 'https://daba-syndic.com';
