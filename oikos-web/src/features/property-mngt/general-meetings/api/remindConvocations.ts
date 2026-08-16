import { httpClient } from '@/shared/api/httpClient';

/** Targets the lots reached but still silent - never those whose convocation never went out. */
export async function remindConvocations(meetingId: string): Promise<{ remindedCount: number }> {
  const { data } = await httpClient.post<{ remindedCount: number }>(
    `/general-meetings/${meetingId}/convocations/reminders`,
  );
  return data;
}
