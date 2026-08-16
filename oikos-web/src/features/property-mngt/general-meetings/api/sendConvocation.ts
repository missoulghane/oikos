import { httpClient } from '@/shared/api/httpClient';
import type { ChannelCode, Convocation } from '@/features/property-mngt/general-meetings/types/convocation.types';

/** Performs the send. The API refuses a channel it cannot perform - those are recorded instead. */
export async function sendConvocation(convocationId: string, channel: ChannelCode): Promise<Convocation> {
  const { data } = await httpClient.post<Convocation>(`/convocations/${convocationId}/send`, { channel });
  return data;
}
