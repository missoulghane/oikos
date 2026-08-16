import type { Paged } from '@/shared/types/pagination.types';

export type MeetingType = 'ORDINARY' | 'EXTRAORDINARY';

export type MeetingStatus =
  | 'DRAFT'
  | 'SCHEDULED'
  | 'CONVENED'
  | 'IN_PROGRESS'
  | 'CLOSED'
  | 'MINUTES_PUBLISHED';

export type VenueType = 'PHYSICAL' | 'VIDEOCONFERENCE' | 'HYBRID';

export type VotingWeightMode = 'PER_UNIT' | 'SHARES';

export type MajorityRule = 'SIMPLE' | 'ABSOLUTE' | 'UNANIMITY';

export type VoteSessionStatus = 'NOT_OPENED' | 'OPEN' | 'CLOSED';

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
  votingWeightMode: VotingWeightMode;
  /** The syndic's note of intent, rich-text HTML. Never rendered without sanitizing. */
  comment: string | null;
  openedWithoutQuorum: boolean;
  agendaItemCount: number;
  createdDate: string;
}

export type PagedGeneralMeetings = Paged<GeneralMeeting>;

export interface GeneralMeetingFilters {
  status?: MeetingStatus;
  type?: MeetingType;
}

export interface CreateGeneralMeetingPayload {
  meetingType: MeetingType;
  title: string;
  scheduledAt?: string | null;
  venueType?: VenueType | null;
  venueAddress?: string | null;
  venueLink?: string | null;
}

/** Full replacement of the editable fields - omitting a field clears it (the API uses PUT). */
export interface UpdateGeneralMeetingPayload {
  meetingType: MeetingType;
  title: string;
  scheduledAt?: string | null;
  venueType?: VenueType | null;
  venueAddress?: string | null;
  venueLink?: string | null;
}

export interface ScheduleGeneralMeetingPayload {
  scheduledAt: string;
  venueType: VenueType;
  venueAddress?: string | null;
  venueLink?: string | null;
}

export interface AgendaItem {
  id: string;
  generalMeetingId: string;
  label: string;
  description: string | null;
  position: number;
  majorityRule: MajorityRule;
  voteSessionStatus: VoteSessionStatus;
  createdDate: string;
}

export interface AgendaItemPayload {
  label: string;
  description?: string | null;
  majorityRule: MajorityRule;
}

export interface MeetingQuorumSetting {
  propertyId: string;
  meetingType: MeetingType;
  quorumPercentage: number;
}
