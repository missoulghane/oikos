/**
 * The AG types the owner space needs. Redeclared here rather than shared with
 * oikos-web: the two apps are deliberate architectural clones, not a shared
 * package, and mobile has no property-mngt feature to import from.
 */
export type MeetingType = 'ORDINARY' | 'EXTRAORDINARY';

export type MeetingStatus =
  | 'DRAFT'
  | 'SCHEDULED'
  | 'CONVENED'
  | 'IN_PROGRESS'
  | 'CLOSED'
  | 'MINUTES_PUBLISHED';

export type VenueType = 'PHYSICAL' | 'VIDEOCONFERENCE' | 'HYBRID';

export type MajorityRule = 'SIMPLE' | 'ABSOLUTE' | 'UNANIMITY';

export type VoteSessionStatus = 'NOT_OPENED' | 'OPEN' | 'CLOSED';

export type AttendanceReply = 'ATTENDING' | 'NOT_ATTENDING' | 'NO_REPLY';

export type VoteOutcome = 'ADOPTED' | 'REJECTED';

export type MinutesStatus = 'DRAFT' | 'UNDER_REVIEW' | 'PUBLISHED';

/** One row per lot owned and per meeting that lot is convoked to. */
export interface MyConvocation {
  id: string;
  generalMeetingId: string;
  propertyName: string;
  meetingTitle: string;
  meetingType: MeetingType;
  meetingStatus: MeetingStatus;
  scheduledAt: string | null;
  venueType: VenueType | null;
  venueAddress: string | null;
  venueLink: string | null;
  unitNumber: string | null;
  buildingName: string | null;
  attendanceReply: AttendanceReply;
  checkedIn: boolean;
}

export interface GeneralMeeting {
  id: string;
  propertyId: string;
  propertyName: string;
  meetingType: MeetingType;
  status: MeetingStatus;
  title: string;
  scheduledAt: string | null;
  venueType: VenueType | null;
  venueAddress: string | null;
  venueLink: string | null;
  quorumPercentage: number;
  /** The syndic's note of intent, rich-text HTML. Rendered as native blocks, never as markup. */
  comment: string | null;
  agendaItemCount: number;
}

export interface AgendaItem {
  id: string;
  generalMeetingId: string;
  label: string;
  description: string | null;
  position: number;
  majorityRule: MajorityRule;
  voteSessionStatus: VoteSessionStatus;
}

export interface AgendaItemResult {
  agendaItemId: string;
  label: string;
  majorityRule: MajorityRule;
  voteSessionStatus: VoteSessionStatus;
  forCount: number;
  againstCount: number;
  abstentionCount: number;
  forWeight: number;
  againstWeight: number;
  abstentionWeight: number;
  expressedWeight: number;
  presentWeight: number;
  totalWeight: number;
  outcome: VoteOutcome;
}

export interface MeetingMinutes {
  id: string;
  generalMeetingId: string;
  /** HTML produced by the API - rendered as blocks on mobile, see htmlToBlocks. */
  content: string;
  status: MinutesStatus;
  publishedAt: string | null;
}
