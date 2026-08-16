import { httpClient } from '@/shared/api/httpClient';
import type { Convocation } from '@/features/property-mngt/general-meetings/types/convocation.types';

/** A room is ticked off by hand, and hands slip. */
export async function undoCheckIn(convocationId: string): Promise<Convocation> {
  const { data } = await httpClient.delete<Convocation>(`/convocations/${convocationId}/check-in`);
  return data;
}
