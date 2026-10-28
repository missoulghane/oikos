/**
 * Une URL par étape : le wizard est ainsi profond-linkable, le bouton
 * « Modifier » du récapitulatif n'est qu'une navigation, et le retour arrière du
 * navigateur fait ce qu'on attend de lui.
 *
 * Les slugs survivent aux réordonnancements : un brouillon en cours ou un lien
 * vers une étape doit continuer de mener au même écran. L'ordre, lui, suit la
 * logique du choix - ce qu'on gère (types), comment on le facture (mode), puis
 * combien (montants en forfait, budget en tantièmes). Cette dernière étape existe
 * dans les deux modes : la sauter en tantièmes ferait changer le total d'étapes
 * au moment même où l'on change de mode.
 */
export const ONBOARDING_BASE_PATH = '/register/board-admin';

export const ONBOARDING_STEPS = [
  { slug: 'account', label: 'Votre compte' },
  { slug: 'property', label: 'Votre copropriété' },
  { slug: 'unit-types', label: 'Types de lots' },
  { slug: 'dues-mode', label: 'Mode de gestion' },
  { slug: 'pricing', label: 'Montants' },
  { slug: 'buildings', label: 'Bâtiments et lots' },
  { slug: 'bank-accounts', label: 'Comptes bancaires' },
  { slug: 'summary', label: 'Récapitulatif' },
] as const;

export type OnboardingStepSlug = (typeof ONBOARDING_STEPS)[number]['slug'];

export const ONBOARDING_DONE_PATH = `${ONBOARDING_BASE_PATH}/done`;

export function stepPath(slug: OnboardingStepSlug): string {
  return `${ONBOARDING_BASE_PATH}/${slug}`;
}

export function stepIndexOf(slug: string): number {
  return ONBOARDING_STEPS.findIndex((step) => step.slug === slug);
}

/** Étape suivante, ou null sur la dernière (le récapitulatif mène à l'écran final). */
export function nextStepPath(slug: OnboardingStepSlug): string | null {
  const index = stepIndexOf(slug);
  const next = ONBOARDING_STEPS[index + 1];
  return next ? stepPath(next.slug) : null;
}

export function previousStepPath(slug: OnboardingStepSlug): string | null {
  const index = stepIndexOf(slug);
  const previous = index > 0 ? ONBOARDING_STEPS[index - 1] : undefined;
  return previous ? stepPath(previous.slug) : null;
}
