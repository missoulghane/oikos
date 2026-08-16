export type MinutesStatus = 'DRAFT' | 'UNDER_REVIEW' | 'PUBLISHED';

export interface MeetingMinutes {
  id: string;
  generalMeetingId: string;
  /** HTML: the minutes are written in a rich-text editor and printed to PDF. */
  content: string;
  status: MinutesStatus;
  publishedAt: string | null;
  createdDate: string;
}
