import { httpClient } from '@/shared/api/httpClient';
import type { ConvocationConfirmation } from '@/features/property-ownership/general-meetings/types/convocationConfirmation.types';

/** Anonymous: no account is involved, the token in the URL is the whole credential. */
export async function getConvocationConfirmation(token: string): Promise<ConvocationConfirmation> {
  const { data } = await httpClient.get<ConvocationConfirmation>(
    `/convocations/by-token/${encodeURIComponent(token)}`,
  );
  return data;
}
