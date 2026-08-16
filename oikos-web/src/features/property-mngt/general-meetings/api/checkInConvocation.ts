import { httpClient } from '@/shared/api/httpClient';
import type { AttendanceMode, Convocation } from '@/features/property-mngt/general-meetings/types/convocation.types';

export async function checkInConvocation(
  convocationId: string,
  attendanceMode: AttendanceMode,
): Promise<Convocation> {
  const { data } = await httpClient.post<Convocation>(`/convocations/${convocationId}/check-in`, { attendanceMode });
  return data;
}
