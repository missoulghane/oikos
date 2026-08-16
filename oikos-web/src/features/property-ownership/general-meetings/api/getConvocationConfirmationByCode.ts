import { httpClient } from '@/shared/api/httpClient';
import type { ConvocationConfirmation } from '@/features/property-ownership/general-meetings/types/convocationConfirmation.types';

/**
 * The paper path: the two six-character codes printed on the letter, for
 * whoever has no phone to scan the QR code.
 *
 * <p>Both codes go in the path, and the API normalises their case - they are
 * printed in capitals for legibility and will be typed either way.
 */
export async function getConvocationConfirmationByCode(
  meetingReference: string,
  code: string,
): Promise<ConvocationConfirmation> {
  const { data } = await httpClient.get<ConvocationConfirmation>(
    `/convocations/by-token/by-reference/${encodeURIComponent(meetingReference)}/${encodeURIComponent(code)}`,
  );
  return data;
}
