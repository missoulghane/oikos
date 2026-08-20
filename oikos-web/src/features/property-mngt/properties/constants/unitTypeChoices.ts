/**
 * LE paramètre applicatif des types de lot : la liste proposée partout où on en
 * choisit - le wizard d'inscription d'un syndic et l'onglet Configuration d'une
 * copropriété. Ajouter un type, le retirer ou les réordonner se fait ici, à un
 * seul endroit, et les deux écrans suivent.
 *
 * <p>Une liste fermée plutôt qu'une saisie libre : deux copropriétés qui
 * écrivent « Box » et « box » ne se comparent plus, et un type mal orthographié
 * suit ensuite chaque appel de charges. Les copropriétés qui portent déjà un
 * type hors liste le gardent - l'onglet Configuration continue de l'afficher.
 *
 * <p>Le pluriel sert aux récapitulatifs (« 12 appartements · 8 box »), où le nom
 * au singulier sonnerait faux.
 */
export const UNIT_TYPE_CHOICES = [
  { name: 'Appartement', plural: 'appartements' },
  { name: 'Parking', plural: 'parkings' },
  { name: 'Box', plural: 'box' },
  { name: 'Bureau', plural: 'bureaux' },
] as const;

export type UnitTypeName = (typeof UNIT_TYPE_CHOICES)[number]['name'];
