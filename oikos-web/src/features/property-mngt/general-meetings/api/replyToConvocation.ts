import { httpClient } from '@/shared/api/httpClient';
import type { AttendanceReply, Convocation } from '@/features/property-mngt/general-meetings/types/convocation.types';

/**
 * Used by the syndic on any lot, and by the copropriétaire on their own (same
 * endpoint). No source is sent: the server settles it from who is calling, so
 * that "the copropriétaire confirmed from the app" cannot simply be claimed.
 * note records how the answer reached the office, when the syndic enters it.
 */
export async function replyToConvocation(
  convocationId: string,
  attendanceReply: AttendanceReply,
  note?: string,
): Promise<Convocation> {
  const { data } = await httpClient.put<Convocation>(`/convocations/${convocationId}/reply`, {
    attendanceReply,
    note: note?.trim() ? note.trim() : null,
  });
  return data;
}
