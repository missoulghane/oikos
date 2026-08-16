import { httpClient } from '@/shared/api/httpClient';
import type { AttendanceReply } from '@/features/property-mngt/general-meetings/types/convocation.types';
import type { ConvocationConfirmation } from '@/features/property-ownership/general-meetings/types/convocationConfirmation.types';

export async function confirmConvocationByCode(
  meetingReference: string,
  code: string,
  attendanceReply: AttendanceReply,
): Promise<ConvocationConfirmation> {
  const { data } = await httpClient.put<ConvocationConfirmation>(
    `/convocations/by-token/by-reference/${encodeURIComponent(meetingReference)}/${encodeURIComponent(code)}/reply`,
    { attendanceReply },
  );
  return data;
}
