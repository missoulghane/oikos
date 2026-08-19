/**
 * Indicatifs proposés par le sélecteur. Liste courte et assumée plutôt que les
 * ~250 indicatifs du monde : ce sont les pays d'où viennent les copropriétaires
 * et les cabinets clients (Maroc en tête), et une liste qu'on lit d'un coup
 * d'oeil bat une liste exhaustive qu'il faut fouiller. En ajouter un est une
 * ligne.
 */
export interface DialCode {
  /** Code ISO 3166-1 alpha-2, clé stable de l'entrée (deux pays partagent un indicatif). */
  iso: string;
  name: string;
  /** Indicatif international, '+' compris. */
  dial: string;
  flag: string;
}

export const DIAL_CODES: DialCode[] = [
  { iso: 'MA', name: 'Maroc', dial: '+212', flag: '🇲🇦' },
  { iso: 'FR', name: 'France', dial: '+33', flag: '🇫🇷' },
  { iso: 'ES', name: 'Espagne', dial: '+34', flag: '🇪🇸' },
  { iso: 'BE', name: 'Belgique', dial: '+32', flag: '🇧🇪' },
  { iso: 'IT', name: 'Italie', dial: '+39', flag: '🇮🇹' },
  { iso: 'PT', name: 'Portugal', dial: '+351', flag: '🇵🇹' },
  { iso: 'DE', name: 'Allemagne', dial: '+49', flag: '🇩🇪' },
  { iso: 'NL', name: 'Pays-Bas', dial: '+31', flag: '🇳🇱' },
  { iso: 'CH', name: 'Suisse', dial: '+41', flag: '🇨🇭' },
  { iso: 'GB', name: 'Royaume-Uni', dial: '+44', flag: '🇬🇧' },
  { iso: 'US', name: 'États-Unis', dial: '+1', flag: '🇺🇸' },
  { iso: 'CA', name: 'Canada', dial: '+1', flag: '🇨🇦' },
  { iso: 'DZ', name: 'Algérie', dial: '+213', flag: '🇩🇿' },
  { iso: 'TN', name: 'Tunisie', dial: '+216', flag: '🇹🇳' },
  { iso: 'MR', name: 'Mauritanie', dial: '+222', flag: '🇲🇷' },
  { iso: 'SN', name: 'Sénégal', dial: '+221', flag: '🇸🇳' },
  { iso: 'CI', name: "Côte d'Ivoire", dial: '+225', flag: '🇨🇮' },
  { iso: 'AE', name: 'Émirats arabes unis', dial: '+971', flag: '🇦🇪' },
  { iso: 'SA', name: 'Arabie saoudite', dial: '+966', flag: '🇸🇦' },
  { iso: 'QA', name: 'Qatar', dial: '+974', flag: '🇶🇦' },
];

export const DEFAULT_DIAL_CODE = DIAL_CODES[0];

/**
 * Découpe un numéro stocké (« +212612345678 ») en indicatif + partie nationale.
 * L'indicatif le plus long gagne : sans cela « +1 » capturerait des numéros
 * commençant par +1xx qui appartiennent à un autre pays.
 *
 * <p>Le paramètre accepte `undefined` alors que le champ est typé `string` :
 * react-hook-form rend la valeur d'un défaut absent, et un formulaire dont le
 * champ manque (brouillon repris d'une version antérieure) faisait tomber tout
 * l'écran sur un `.trim()`. Une saisie vide est un cas normal, pas une panne.
 */
export function splitPhone(value: string | null | undefined): { iso: string; nationalNumber: string } {
  const trimmed = (value ?? '').trim();
  const match = [...DIAL_CODES]
    .sort((a, b) => b.dial.length - a.dial.length)
    .find((code) => trimmed.startsWith(code.dial));
  if (!match) {
    return { iso: DEFAULT_DIAL_CODE.iso, nationalNumber: trimmed.replace(/^\+/, '') };
  }
  return { iso: match.iso, nationalNumber: trimmed.slice(match.dial.length) };
}

/**
 * Recompose la valeur stockée. Les zéros de tête de la partie nationale sont
 * retirés : on les écrit par réflexe (« 0612... ») alors qu'ils n'existent pas
 * en format international, et « +2120612... » n'est joignable nulle part.
 */
export function composePhone(dial: string, nationalNumber: string): string {
  const digits = nationalNumber.replace(/\D/g, '').replace(/^0+/, '');
  return digits === '' ? '' : `${dial}${digits}`;
}
