import { httpClient } from '@/shared/api/httpClient';
import type { UnitTypeChoice } from '@/shared/types/referenceCatalogs.types';

/**
 * Les types de lot proposés (table unit_type_choice), dans l'ordre d'affichage -
 * le wizard d'inscription et l'onglet Configuration lisent la même liste.
 */
export async function getUnitTypeChoices(): Promise<UnitTypeChoice[]> {
  const { data } = await httpClient.get<UnitTypeChoice[]>('/unit-type-choices');
  return data;
}
