import { httpClient } from '@/shared/api/httpClient';
import type {
  AttendanceMode,
  AttendanceReply,
  Convocation,
} from '@/features/property-mngt/general-meetings/types/convocation.types';

export interface ReplyToConvocationInput {
  attendanceReply: AttendanceReply;
  /**
   * How the lot announced it would attend, and whether a stand-in was
   * announced. Both are dropped server-side on an "absent" and on an answer the
   * owner gave from their own space. Neither grants anything: presence is the
   * check-in, and byProxy is an announcement, not a mandate.
   */
  attendanceMode?: AttendanceMode | null;
  byProxy?: boolean;
  /**
   * By what means the answer reached the office. Sent, unlike the source, and
   * the contrast is deliberate: only the person who took the call knows it, so
   * it is a declaration. The server ignores it when it settles the source as
   * anything other than OTHER.
   */
  medium?: string | null;
  /** When the answer was given, if that is not now - a letter read on Thursday, dated Tuesday. */
  receivedAt?: string | null;
  note?: string | null;
}

/**
 * Used by the syndic on any lot, and by the copropriétaire on their own (same
 * endpoint). No source is sent: the server settles it from who is calling, so
 * that "the copropriétaire confirmed from the app" cannot simply be claimed.
 */
export async function replyToConvocation(
  convocationId: string,
  input: ReplyToConvocationInput,
): Promise<Convocation> {
  const { data } = await httpClient.put<Convocation>(`/convocations/${convocationId}/reply`, {
    attendanceReply: input.attendanceReply,
    attendanceMode: input.attendanceMode ?? null,
    byProxy: input.byProxy ?? false,
    medium: input.medium?.trim() ? input.medium.trim() : null,
    receivedAt: input.receivedAt ?? null,
    note: input.note?.trim() ? input.note.trim() : null,
  });
  return data;
}
