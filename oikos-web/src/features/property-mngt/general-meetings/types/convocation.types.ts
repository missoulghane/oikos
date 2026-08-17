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

/**
 * How the confirmation was obtained. Settled server-side from the caller, never
 * sent. OTHER means neither of the two application paths was taken - by what
 * means it then arrived is `replyMediumCode`, which IS declared by the client
 * and is a separate kind of fact.
 */
export type ReplySource = 'OWNER_APP' | 'OWNER_LINK' | 'OTHER';

/** A row of the server's reply-medium catalog, read from GET /reply-media. */
export interface ReplyMedium {
  code: string;
  label: string;
  position: number;
}

/**
 * One answer given for the lot. mediumLabel is resolved by the server against
 * the catalog, so a new medium displays correctly without touching this app.
 *
 * receivedAt is when the answer was given - declared, and freely backdated;
 * recordedAt is when it was typed. Both show, because that is what makes a
 * backdated entry legible: "reçue le 13, saisie le 15".
 */
export interface ConvocationReply {
  id: string;
  attendanceReply: AttendanceReply;
  /** How the lot announced it would attend. Null unless attending. */
  attendanceMode: AttendanceMode | null;
  /** A stand-in was announced. Not the mandate itself - proxies are still out of scope. */
  byProxy: boolean;
  source: ReplySource;
  mediumCode: string | null;
  mediumLabel: string | null;
  note: string | null;
  receivedAt: string;
  recordedAt: string | null;
}

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
  /**
   * A chase rather than the convocation itself. Display only - the convocation's
   * own status and its sentAt ignore it, so a reminder never moves the date the
   * notice period runs from.
   */
  reminder: boolean;
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
  /**
   * Every answer ever given, newest first - withdrawals included. An audit
   * trail, never the authority: what counts is `attendanceReply` below, which
   * is a copy of this list's head.
   */
  replies: ConvocationReply[];
  /** The FIRST successful send - the date the notice period runs from. */
  sentAt: string | null;
  deliveryStatus: DeliveryStatus;
  attendanceReply: AttendanceReply;
  repliedAt: string | null;
  replySource: ReplySource | null;
  /** By what means the standing answer reached the office. Null unless the source is OTHER. */
  replyMediumCode: string | null;
  replyMediumLabel: string | null;
  replyNote: string | null;
  /** Announced, not constated - `attendanceMode` below is the check-in. */
  replyAttendanceMode: AttendanceMode | null;
  replyByProxy: boolean;
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
