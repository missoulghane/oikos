import { httpClient } from '@/shared/api/httpClient';
import type { ConvocationChannel } from '@/features/property-mngt/general-meetings/types/convocation.types';

/**
 * The channels on offer, in display order. Fetched rather than hardcoded: the
 * server keeps the catalog precisely so a new channel needs no release here.
 */
export async function listConvocationChannels(): Promise<ConvocationChannel[]> {
  const { data } = await httpClient.get<ConvocationChannel[]>('/convocation-channels');
  return data;
}
