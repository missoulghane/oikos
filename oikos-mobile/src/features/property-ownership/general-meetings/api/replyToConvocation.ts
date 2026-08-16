import { httpClient } from '@/shared/api/httpClient';
import type { AttendanceReply } from '@/features/property-ownership/general-meetings/types/generalMeeting.types';

/** Same endpoint the syndic uses; the API gates it per lot through ownsUnit. */
export async function replyToConvocation(convocationId: string, attendanceReply: AttendanceReply): Promise<void> {
  await httpClient.put(`/convocations/${convocationId}/reply`, { attendanceReply });
}
