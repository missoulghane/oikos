import { httpClient } from '@/shared/api/httpClient';
import type { AttendanceReply } from '@/features/property-mngt/general-meetings/types/convocation.types';
import type { ConvocationConfirmation } from '@/features/property-ownership/general-meetings/types/convocationConfirmation.types';

/**
 * Anonymous write - the one in the product. No source is sent: reaching this
 * endpoint is itself what tells the server the answer came through the link.
 *
 * <p>The link is not enough on its own: the answer carries the six-character
 * code of the lot, printed beside the QR code on the convocation. The API
 * checks it and refuses the answer without it, so this is not a formality the
 * page could skip.
 */
export async function confirmConvocation(
  token: string,
  attendanceReply: AttendanceReply,
  confirmationCode: string,
): Promise<ConvocationConfirmation> {
  const { data } = await httpClient.put<ConvocationConfirmation>(
    `/convocations/by-token/${encodeURIComponent(token)}/reply`,
    { attendanceReply, confirmationCode },
  );
  return data;
}
