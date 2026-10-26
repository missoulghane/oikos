/**
 * Sièges proposés au syndic bénévole à l'étape 1 du wizard - miroir de
 * BoardSeat côté API, volontairement plus étroit que les BoardRole du conseil
 * syndical : PROPERTY_MANAGER (« Gestionnaire ») est le siège d'un cabinet
 * professionnel, qui ne passe jamais par ce wizard.
 *
 * <p>VOLUNTEER_MANAGER en tête et par défaut : c'est le « Syndic bénévole » de
 * la nomenclature, le cas de loin le plus courant.
 */
export const BOARD_SEATS = ['VOLUNTEER_MANAGER', 'PRESIDENT', 'TREASURER', 'SECRETARY', 'MEMBER'] as const;

export type BoardSeat = (typeof BOARD_SEATS)[number];

export const DEFAULT_BOARD_SEAT: BoardSeat = 'VOLUNTEER_MANAGER';

export const BOARD_SEAT_LABELS: Record<BoardSeat, string> = {
  VOLUNTEER_MANAGER: 'Syndic',
  PRESIDENT: 'Président',
  TREASURER: 'Trésorier',
  SECRETARY: 'Secrétaire',
  MEMBER: 'Membre du conseil syndical',
};
