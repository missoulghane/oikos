import { httpClient } from '@/shared/api/httpClient';
import type { AttendanceReply } from '@/features/property-mngt/general-meetings/types/convocation.types';
import type { ConvocationConfirmation } from '@/features/property-ownership/general-meetings/types/convocationConfirmation.types';

/**
 * Anonymous write - the one in the product. No source is sent: reaching this
 * endpoint is itself what tells the server the answer came through the link.
 */
export async function confirmConvocation(
  token: string,
  attendanceReply: AttendanceReply,
): Promise<ConvocationConfirmation> {
  const { data } = await httpClient.put<ConvocationConfirmation>(
    `/convocations/by-token/${encodeURIComponent(token)}/reply`,
    { attendanceReply },
  );
  return data;
}
