/**
 * A channel code, not a union of literals: the catalog is data server-side so
 * that adding one is an INSERT rather than a deployment, and a closed union
 * here would put the deployment straight back.
 */
export type ChannelCode = string;

/** A row of the server's channel catalog, read from GET /convocation-channels. */
export interface ConvocationChannel {
  code: ChannelCode;
  label: string;
  /** The application performs it itself; the others are recorded once a person has acted. */
  automated: boolean;
  position: number;
}

/** On a convocation it is derived from its deliveries; a delivery only ever carries the last two. */
export type DeliveryStatus = 'TO_SEND' | 'SENT' | 'FAILED';

export type AttendanceReply = 'ATTENDING' | 'NOT_ATTENDING' | 'NO_REPLY';

export type AttendanceMode = 'ON_SITE' | 'REMOTE';

/** How the confirmation was obtained. Settled server-side from the caller, never sent. */
export type ReplySource = 'OWNER_APP' | 'OWNER_LINK' | 'SYNDIC_OFFICE';

/** Derived server-side from the three groups of fields below - never sent back. */
export type ConvocationStatus = 'TO_SEND' | 'SENT' | 'CONFIRMED' | 'CHECKED_IN';

/** email is null when the party has none recorded - which is why a row can end up FAILED. */
export interface ConvocationRecipient {
  fullName: string;
  email: string | null;
}

/**
 * One attempt at getting the convocation out, on one channel. channelLabel is
 * resolved by the server against the catalog, so a new channel displays
 * correctly without touching this app.
 */
export interface ConvocationDelivery {
  id: string;
  channelCode: ChannelCode;
  channelLabel: string;
  status: DeliveryStatus;
  sentAt: string | null;
  reference: string | null;
}

export interface Convocation {
  id: string;
  generalMeetingId: string;
  unitId: string;
  unitNumber: string | null;
  buildingName: string | null;
  recipients: ConvocationRecipient[];
  votingWeight: number;
  /** Every attempt, oldest first. Empty until something is sent. */
  deliveries: ConvocationDelivery[];
  /** The FIRST successful send - the date the notice period runs from. */
  sentAt: string | null;
  deliveryStatus: DeliveryStatus;
  attendanceReply: AttendanceReply;
  repliedAt: string | null;
  replySource: ReplySource | null;
  replyNote: string | null;
  checkedIn: boolean;
  attendanceMode: AttendanceMode | null;
  checkedInAt: string | null;
  status: ConvocationStatus;
  /**
   * Null on every list response, populated only by GET /convocations/{id}. The
   * pair a syndic reads out to a copropriétaire who lost their letter; the API
   * keeps them off the list so a hundred codes never travel together.
   */
  meetingPublicReference: string | null;
  confirmationCode: string | null;
}

export interface AttendanceSummary {
  totalUnits: number;
  sentCount: number;
  attendingCount: number;
  notAttendingCount: number;
  noReplyCount: number;
  checkedInCount: number;
  totalWeight: number;
  presentWeight: number;
  quorumPercentage: number;
  quorumRequired: boolean;
  quorumReached: boolean;
}
