import type { BoardRoleOption, UnitTypeChoice } from '@/shared/types/referenceCatalogs.types';

/** Copie des catalogues semés par V1__baseline.sql (board_role, unit_type_choice), pour les tests. */
export const BOARD_ROLES_FIXTURE: BoardRoleOption[] = [
  { code: 'VOLUNTEER_MANAGER', label: 'Syndic bénévole', position: 1, availableAtSignup: true },
  { code: 'PRESIDENT', label: 'Président', position: 2, availableAtSignup: true },
  { code: 'TREASURER', label: 'Trésorier', position: 3, availableAtSignup: true },
  { code: 'SECRETARY', label: 'Secrétaire', position: 4, availableAtSignup: true },
  { code: 'MEMBER', label: 'Membre du conseil syndical', position: 5, availableAtSignup: true },
  { code: 'PROPERTY_MANAGER', label: 'Gestionnaire', position: 6, availableAtSignup: false },
];

export const UNIT_TYPE_CHOICES_FIXTURE: UnitTypeChoice[] = [
  { name: 'Appartement', plural: 'appartements', description: 'Logements de la copropriété.', position: 1 },
  { name: 'Parking', plural: 'parkings', description: 'Places de stationnement, extérieures ou en sous-sol.', position: 2 },
  { name: 'Box', plural: 'box', description: 'Garages fermés.', position: 3 },
  { name: 'Bureau', plural: 'bureaux', description: 'Locaux professionnels.', position: 4 },
];
