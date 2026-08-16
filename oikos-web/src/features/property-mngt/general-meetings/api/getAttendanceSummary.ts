import { httpClient } from '@/shared/api/httpClient';
import type { AttendanceSummary } from '@/features/property-mngt/general-meetings/types/convocation.types';

export async function getAttendanceSummary(meetingId: string): Promise<AttendanceSummary> {
  const { data } = await httpClient.get<AttendanceSummary>(`/general-meetings/${meetingId}/attendance-summary`);
  return data;
}
