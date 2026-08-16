import type {
  AttendanceReply,
  MajorityRule,
  MeetingStatus,
  MeetingType,
  VoteOutcome,
} from '@/features/property-ownership/general-meetings/types/generalMeeting.types';

type BadgeColor = 'primary' | 'success' | 'error' | 'warning' | 'info' | 'light' | 'dark';

export const MEETING_STATUS_LABELS: Record<MeetingStatus, string> = {
  DRAFT: 'Brouillon',
  SCHEDULED: 'Planifiée',
  CONVENED: 'Convoquée',
  IN_PROGRESS: 'En cours',
  CLOSED: 'Clôturée',
  MINUTES_PUBLISHED: 'PV publié',
};

export const MEETING_STATUS_BADGE_COLORS: Record<MeetingStatus, BadgeColor> = {
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

export const ATTENDANCE_REPLY_LABELS: Record<AttendanceReply, string> = {
  ATTENDING: 'Présent',
  NOT_ATTENDING: 'Absent',
  NO_REPLY: 'Sans réponse',
};

/** The denominator is part of the label: it is what decides an outcome. */
export const MAJORITY_RULE_LABELS: Record<MajorityRule, string> = {
  SIMPLE: 'Majorité simple (voix exprimées)',
  ABSOLUTE: 'Majorité absolue (voix de la copropriété)',
  UNANIMITY: 'Unanimité (lots présents)',
};

export const VOTE_OUTCOME_LABELS: Record<VoteOutcome, string> = {
  ADOPTED: 'Adoptée',
  REJECTED: 'Rejetée',
};
