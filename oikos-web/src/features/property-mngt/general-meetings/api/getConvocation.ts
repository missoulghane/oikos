import { httpClient } from '@/shared/api/httpClient';
import type { Convocation } from '@/features/property-mngt/general-meetings/types/convocation.types';

/**
 * One convocation, fetched on its own rather than picked out of the cached
 * list. The list deliberately carries no confirmation code - a hundred of them
 * in one payload is the whole copropriété's answers on one screen - so the
 * detail page is the only place that can show it, and it has to ask.
 */
export async function getConvocation(convocationId: string): Promise<Convocation> {
  const { data } = await httpClient.get<Convocation>(`/convocations/${convocationId}`);
  return data;
}
