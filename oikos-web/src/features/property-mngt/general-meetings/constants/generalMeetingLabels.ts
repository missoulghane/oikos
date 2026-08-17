import type {
  MajorityRule,
  MeetingStatus,
  MeetingType,
  VenueType,
  VoteSessionStatus,
  VotingWeightMode,
} from '@/features/property-mngt/general-meetings/types/generalMeeting.types';
import type {
  AttendanceMode,
  AttendanceReply,
  ConvocationStatus,
  DeliveryStatus,
  ReplySource,
} from '@/features/property-mngt/general-meetings/types/convocation.types';
import type { MinutesStatus } from '@/features/property-mngt/general-meetings/types/minutes.types';
import type { VoteChoice, VoteOutcome } from '@/features/property-mngt/general-meetings/types/vote.types';

type BadgeColor = 'primary' | 'success' | 'error' | 'warning' | 'info' | 'light' | 'dark';

export const MEETING_STATUS_LABELS: Record<MeetingStatus, string> = {
  DRAFT: 'Brouillon',
  SCHEDULED: 'Planifiée',
  CONVENED: 'Convoquée',
  IN_PROGRESS: 'En cours',
  CLOSED: 'Clôturée',
  MINUTES_PUBLISHED: 'PV publié',
};

export const MEETING_STATUS_COLORS: Record<MeetingStatus, BadgeColor> = {
  DRAFT: 'light',
  SCHEDULED: 'info',
  CONVENED: 'primary',
  IN_PROGRESS: 'warning',
  CLOSED: 'dark',
  MINUTES_PUBLISHED: 'success',
};

export const MEETING_TYPE_LABELS: Record<MeetingType, string> = {
  ORDINARY: 'Ordinaire',
  EXTRAORDINARY: 'Extraordinaire',
};

export const VENUE_TYPE_LABELS: Record<VenueType, string> = {
  PHYSICAL: 'Sur place',
  VIDEOCONFERENCE: 'Visioconférence',
  HYBRID: 'Hybride',
};

export const VOTING_WEIGHT_MODE_LABELS: Record<VotingWeightMode, string> = {
  PER_UNIT: 'Une voix par lot',
  SHARES: 'Au prorata des tantièmes',
};

/**
 * The denominator is named in the label, not just the rule: it is what
 * decides an outcome, and a syndic choosing a majority has to see which
 * voices it will be measured against.
 */
export const MAJORITY_RULE_LABELS: Record<MajorityRule, string> = {
  SIMPLE: 'Majorité simple (voix exprimées)',
  ABSOLUTE: 'Majorité absolue (voix de la copropriété)',
  UNANIMITY: 'Unanimité (lots présents)',
};

export const MAJORITY_RULE_SHORT_LABELS: Record<MajorityRule, string> = {
  SIMPLE: 'Simple',
  ABSOLUTE: 'Absolue',
  UNANIMITY: 'Unanimité',
};

export const VOTE_SESSION_STATUS_LABELS: Record<VoteSessionStatus, string> = {
  NOT_OPENED: 'Scrutin non ouvert',
  OPEN: 'Scrutin ouvert',
  CLOSED: 'Scrutin clos',
};

export const VOTE_SESSION_STATUS_COLORS: Record<VoteSessionStatus, BadgeColor> = {
  NOT_OPENED: 'light',
  OPEN: 'warning',
  CLOSED: 'dark',
};

/**
 * No channel labels here on purpose. The catalog lives on the server so that a
 * new channel is an INSERT and not a release; a map in this file would be the
 * release. Labels come with the data - `ConvocationChannel.label` on
 * GET /convocation-channels, `channelLabel` on each delivery.
 */
export const DELIVERY_STATUS_LABELS: Record<DeliveryStatus, string> = {
  TO_SEND: 'À envoyer',
  SENT: 'Envoyée',
  FAILED: 'Échec',
};

/**
 * The five answers a syndic records, as one flat list - and three fields
 * underneath. "Absent, sur place" is not a state of the world, so the screen
 * offers combinations that exist rather than a cartesian product it would then
 * have to police.
 *
 * <p>`byProxy` announces a stand-in; it is NOT a procuration. The mandate - who
 * holds it, whose voice it carries - is still out of scope, and nothing here
 * counts towards a vote: presence is the check-in, on the Séance tab.
 */
export const ATTENDANCE_ANSWERS = [
  { value: 'ATTENDING_ON_SITE', label: 'Présent – Sur place', reply: 'ATTENDING', mode: 'ON_SITE', byProxy: false },
  { value: 'ATTENDING_REMOTE', label: 'Présent – À distance', reply: 'ATTENDING', mode: 'REMOTE', byProxy: false },
  {
    value: 'PROXY_ON_SITE',
    label: 'Présent par procuration – Sur place',
    reply: 'ATTENDING',
    mode: 'ON_SITE',
    byProxy: true,
  },
  {
    value: 'PROXY_REMOTE',
    label: 'Présent par procuration – À distance',
    reply: 'ATTENDING',
    mode: 'REMOTE',
    byProxy: true,
  },
  { value: 'NOT_ATTENDING', label: 'Absent', reply: 'NOT_ATTENDING', mode: null, byProxy: false },
] as const satisfies readonly {
  value: string;
  label: string;
  reply: AttendanceReply;
  mode: AttendanceMode | null;
  byProxy: boolean;
}[];

export type AttendanceAnswerValue = (typeof ATTENDANCE_ANSWERS)[number]['value'];

/**
 * The same five labels, rebuilt from what a stored answer carries - so the
 * history reads exactly like the select that produced it. Falls back to the
 * bare reply for anything recorded before the mode existed, or answered through
 * the confirmation link, which asks for neither.
 */
export function attendanceAnswerLabel(
  reply: AttendanceReply,
  mode: AttendanceMode | null,
  byProxy: boolean,
): string {
  const match = ATTENDANCE_ANSWERS.find(
    (answer) => answer.reply === reply && answer.mode === mode && answer.byProxy === byProxy,
  );
  return match ? match.label : ATTENDANCE_REPLY_LABELS[reply];
}

export const ATTENDANCE_REPLY_LABELS: Record<AttendanceReply, string> = {
  ATTENDING: 'Présent',
  NOT_ATTENDING: 'Absent',
  NO_REPLY: 'Sans réponse',
};

export const ATTENDANCE_MODE_LABELS: Record<AttendanceMode, string> = {
  ON_SITE: 'Sur place',
  REMOTE: 'À distance',
};

/**
 * What the server can actually vouch for, and no more. The first two name a
 * path the application itself witnessed; OTHER says only that neither was
 * taken. By what means the answer then arrived is the medium - declared by the
 * syndic, shown next to this - and the wording keeps the two apart rather than
 * letting a declaration read as something the application observed.
 *
 * No medium labels here, for the same reason there are no channel ones: the
 * catalog lives on the server so that adding an entry is an INSERT and not a
 * release. Labels come with the data - `mediumLabel` on each reply.
 */
export const REPLY_SOURCE_LABELS: Record<ReplySource, string> = {
  OWNER_APP: 'Depuis son espace',
  OWNER_LINK: 'Via le lien reçu',
  OTHER: 'Reçue au bureau',
};

export const CONVOCATION_STATUS_LABELS: Record<ConvocationStatus, string> = {
  TO_SEND: 'À envoyer',
  SENT: 'Envoyée',
  CONFIRMED: 'A répondu',
  CHECKED_IN: 'Émargé',
};

export const CONVOCATION_STATUS_COLORS: Record<ConvocationStatus, BadgeColor> = {
  TO_SEND: 'light',
  SENT: 'info',
  CONFIRMED: 'primary',
  CHECKED_IN: 'success',
};

export const VOTE_CHOICE_LABELS: Record<VoteChoice, string> = {
  FOR: 'Pour',
  AGAINST: 'Contre',
  ABSTENTION: 'Abstention',
};

export const VOTE_CHOICE_COLORS: Record<VoteChoice, BadgeColor> = {
  FOR: 'success',
  AGAINST: 'error',
  ABSTENTION: 'light',
};

export const VOTE_OUTCOME_LABELS: Record<VoteOutcome, string> = {
  ADOPTED: 'Adoptée',
  REJECTED: 'Rejetée',
};

export const MINUTES_STATUS_LABELS: Record<MinutesStatus, string> = {
  DRAFT: 'Brouillon',
  UNDER_REVIEW: 'Validé',
  PUBLISHED: 'Publié',
};

export const MINUTES_STATUS_COLORS: Record<MinutesStatus, BadgeColor> = {
  DRAFT: 'light',
  UNDER_REVIEW: 'warning',
  PUBLISHED: 'success',
};
