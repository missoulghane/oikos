/**
 * Drives WizardShell's progress bar. Unlike oikos-web (one URL per step, so
 * this file also builds paths for deep-linkability and the "Modifier" links),
 * navigation between steps here is plain `navigation.navigate('ScreenName')` /
 * `navigation.goBack()` calls against OnboardingStackParamList - React
 * Navigation's own linking config (see RootNavigator) already gives each step
 * a deep-linkable path, so no path-building helpers are needed on this side.
 */
export const ONBOARDING_STEPS = [
  { screen: 'Account', label: 'Votre compte' },
  { screen: 'Property', label: 'Votre copropriété' },
  // Même ordre que oikos-web : ce qu'on gère (types), comment on le facture
  // (mode), puis combien - montants en forfait, budget en tantièmes. La
  // dernière existe dans les deux modes, pour que le total ne change pas quand
  // on change de mode.
  { screen: 'UnitTypes', label: 'Types de lots' },
  { screen: 'DuesMode', label: 'Mode de gestion' },
  { screen: 'Pricing', label: 'Montants' },
  { screen: 'Buildings', label: 'Bâtiments et lots' },
  { screen: 'BankAccounts', label: 'Comptes bancaires' },
  { screen: 'Summary', label: 'Récapitulatif' },
] as const;

export type OnboardingStepScreen = (typeof ONBOARDING_STEPS)[number]['screen'];

export function stepIndexOf(screen: string): number {
  return ONBOARDING_STEPS.findIndex((step) => step.screen === screen);
}
