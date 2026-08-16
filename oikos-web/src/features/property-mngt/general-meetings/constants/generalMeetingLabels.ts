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
 * Named from the copropriétaire's side rather than the syndic's: "au bureau du
 * syndic" is what a reader needs to tell a confirmation the owner gave from
 * one the office wrote down for them.
 */
export const REPLY_SOURCE_LABELS: Record<ReplySource, string> = {
  OWNER_APP: 'Depuis son espace',
  OWNER_LINK: 'Via le lien reçu',
  SYNDIC_OFFICE: 'Au bureau du syndic',
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
