import { httpClient } from '@/shared/api/httpClient';
import type { Convocation } from '@/features/property-mngt/general-meetings/types/convocation.types';

/**
 * Creates one convocation per lot and convokes the meeting. Sends nothing -
 * that is a separate action (sendPendingConvocations). Idempotent: find-or-create
 * per lot server-side, so a double click adds nothing.
 */
export async function generateConvocations(meetingId: string): Promise<Convocation[]> {
  const { data } = await httpClient.post<Convocation[]>(`/general-meetings/${meetingId}/convocations`);
  return data;
}
