import type { AttendanceReply } from '@/features/property-mngt/general-meetings/types/convocation.types';
import type { VenueType } from '@/features/property-mngt/general-meetings/types/generalMeeting.types';

/**
 * What the confirmation link exposes, and all it exposes. Deliberately carries
 * no identifier - not the convocation's, not the lot's, not the meeting's: the
 * page is anonymous, the token is the only handle, and the server keeps the
 * payload this narrow so a leaked link leaks one lot's convocation and nothing
 * of the copropriété.
 */
export interface ConvocationConfirmation {
  propertyName: string;
  meetingTitle: string;
  meetingType: string;
  scheduledAt: string | null;
  venueType: VenueType | null;
  venueAddress: string | null;
  venueLink: string | null;
  unitNumber: string | null;
  buildingName: string | null;
  attendanceReply: AttendanceReply;
  repliedAt: string | null;
  /** False once the session has opened: presence is then the émargement, not a declaration. */
  stillOpen: boolean;
}
