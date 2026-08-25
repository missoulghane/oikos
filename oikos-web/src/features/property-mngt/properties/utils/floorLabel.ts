/**
 * Nomme un étage comme on le dit : 0 est le rez-de-chaussée (jamais « étage 0 »),
 * 1 le premier. Une seule fonction pour la saisie et pour l'affichage, sans quoi
 * le même étage se lirait de deux façons selon l'écran.
 */
export function floorLabel(floor: number): string {
  if (floor === 0) {
    return 'Rez-de-chaussée';
  }
  return floor === 1 ? '1er étage' : `${floor}e étage`;
}
