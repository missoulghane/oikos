import { httpClient } from '@/shared/api/httpClient';
import type { ChannelCode } from '@/features/property-mngt/general-meetings/types/convocation.types';

export interface SendConvocationsResult {
  sentCount: number;
  failedCount: number;
}

/**
 * Sends every convocation still waiting to go out. Re-running retries exactly
 * those and never re-sends one that already left. Failures are counted, not
 * thrown: one unreachable lot must not fail the run.
 */
export async function sendPendingConvocations(
  meetingId: string,
  channel: ChannelCode,
): Promise<SendConvocationsResult> {
  const { data } = await httpClient.post<SendConvocationsResult>(
    `/general-meetings/${meetingId}/convocations/send`,
    { channel },
  );
  return data;
}
