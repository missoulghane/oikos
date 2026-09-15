import { SUPPORT_EMAIL } from '@/config/env';

/**
 * Tous les messages d'erreur montrés à l'utilisateur, au même endroit.
 *
 * <p>Aucune interface n'affiche jamais le message renvoyé par l'API : il est
 * écrit pour les logs (en anglais, avec des identifiants techniques, parfois une
 * trace d'implémentation), il change au gré des refactorings, et le montrer
 * revient à raconter l'intérieur du serveur à qui le sonde. Les écrans piochent
 * ici, à partir du statut HTTP ou du code d'erreur - voir getErrorMessage.
 *
 * <p>Le corollaire : quand un message générique ne suffit pas à un cas précis,
 * la réponse n'est pas de réafficher le détail de l'exception, c'est d'ajouter un
 * code à l'API (shared/exception/ErrorCodes.java) et sa phrase française ici.
 */

export { SUPPORT_EMAIL };

export const ERROR_MESSAGES = {
  /** Le tout-venant : panne serveur, bug, cas non prévu. Rien à demander à l'utilisateur, sinon de nous écrire. */
  unexpected: `Une erreur inattendue est survenue. Réessayez dans un instant ; si le problème persiste, contactez le support à ${SUPPORT_EMAIL}.`,
  network: 'Impossible de joindre le serveur. Vérifiez votre connexion, puis réessayez.',
  invalidRequest: 'Les informations saisies n’ont pas été acceptées. Vérifiez le formulaire, puis réessayez.',
  forbidden: 'Vous n’avez pas les droits nécessaires pour effectuer cette action.',
  notFound: 'Cet élément est introuvable. Il a peut-être été supprimé entre-temps.',
  conflict:
    'Cette action n’est pas possible dans l’état actuel des données. Rechargez la page, puis réessayez.',
  payloadTooLarge: 'Le fichier est trop volumineux.',
  tooManyRequests: 'Trop de tentatives. Patientez quelques minutes avant de réessayer.',
  invalidLink: 'Ce lien n’est plus valide : il a expiré ou a déjà été utilisé. Demandez-en un nouveau.',
} as const;

/**
 * Connexion. Un seul et même message pour toutes les raisons d'un refus
 * (identifiant inconnu, mot de passe faux, compte verrouillé) : dire laquelle
 * ferait de cette page un annuaire des comptes, où l'on teste des adresses pour
 * savoir lesquelles existent.
 *
 * <p>Seule exception, `accountNotActivated`, que l'API ne renvoie qu'après un mot
 * de passe correct : celui qui le lit connaît déjà le mot de passe et n'apprend
 * donc rien - alors qu'il lui évite de chercher indéfiniment une faute de frappe
 * dans un mot de passe qui était bon.
 */
export const AUTH_ERROR_MESSAGES = {
  invalidCredentials: 'Identifiant ou mot de passe invalide.',
  accountNotActivated:
    'Votre compte n’est pas encore activé. Ouvrez le lien d’activation reçu par email, puis reconnectez-vous.',
  tooManyAttempts: 'Trop de tentatives de connexion. Patientez quelques minutes avant de réessayer.',
} as const;

/**
 * Les seuls cas où le statut HTTP ne suffit pas à choisir la bonne phrase. Le
 * vocabulaire est celui de l'API (ErrorCodes.java).
 */
export const CODE_ERROR_MESSAGES: Record<string, string> = {
  INVALID_CREDENTIALS: AUTH_ERROR_MESSAGES.invalidCredentials,
  ACCOUNT_NOT_ACTIVATED: AUTH_ERROR_MESSAGES.accountNotActivated,
  INVALID_LINK: ERROR_MESSAGES.invalidLink,
};
