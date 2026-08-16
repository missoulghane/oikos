import type {
  MeetingStatus,
  MeetingType,
  VenueType,
} from '@/features/property-mngt/general-meetings/types/generalMeeting.types';
import type { AttendanceReply } from '@/features/property-mngt/general-meetings/types/convocation.types';

/**
 * One row per lot owned and per meeting that lot is convoked to. An owner of
 * three lots in the same copropriété sees three rows for the same AG - which
 * is correct: each lot carries its own voice and its own answer.
 */
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
