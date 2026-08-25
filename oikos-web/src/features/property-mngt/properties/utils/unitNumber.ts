/**
 * Un numéro de lot s'enregistre préfixé : « N° 32 », jamais « 32 » nu. C'est la
 * même forme que celle produite par la génération en masse côté API
 * (Unit.generatedNumber), pour qu'un lot ajouté à la main et un lot généré se
 * lisent pareil dans la liste - « N° 32 — Appartement ».
 *
 * <p>La saisie, elle, ne porte que les chiffres (voir addUnitSchema) : le « N° »
 * est affiché dans le champ, pas retapé par le syndic.
 */
export function unitNumberFromDigits(digits: string): string {
  return `N° ${digits.trim()}`;
}
