import { httpClient } from '@/shared/api/httpClient';
import type { BoardMember } from '@/features/property-mngt/board-members/types/boardMember.types';

export async function getBoardMembers(propertyId: string): Promise<BoardMember[]> {
  const { data } = await httpClient.get<BoardMember[]>(`/properties/${propertyId}/board-members`);
  return data;
}
