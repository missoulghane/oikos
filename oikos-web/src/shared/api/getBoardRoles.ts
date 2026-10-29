import { httpClient } from '@/shared/api/httpClient';
import type { BoardRoleOption } from '@/shared/types/referenceCatalogs.types';

/**
 * Les sièges du conseil syndical, dans l'ordre d'affichage. Lus depuis l'API
 * (table board_role) plutôt qu'écrits ici : l'inscription et « Ajouter un
 * membre » proposent ainsi la même liste, sous les mêmes libellés.
 */
export async function getBoardRoles(): Promise<BoardRoleOption[]> {
  const { data } = await httpClient.get<BoardRoleOption[]>('/board-roles');
  return data;
}
