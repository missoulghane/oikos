import { httpClient } from '@/shared/api/httpClient';
import type { Convocation, ConvocationStatus } from '@/features/property-mngt/general-meetings/types/convocation.types';

export async function listConvocations(meetingId: string, status?: ConvocationStatus): Promise<Convocation[]> {
  const { data } = await httpClient.get<Convocation[]>(`/general-meetings/${meetingId}/convocations`, {
    params: { status },
  });
  return data;
}
