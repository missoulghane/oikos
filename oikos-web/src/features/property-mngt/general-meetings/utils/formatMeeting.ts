import type { GeneralMeeting } from '@/features/property-mngt/general-meetings/types/generalMeeting.types';

const DATE_TIME = new Intl.DateTimeFormat('fr-FR', {
  dateStyle: 'long',
  timeStyle: 'short',
  timeZone: 'Africa/Casablanca',
});

/** An AG still being prepared has no date yet - "à définir" is a state, not a missing value. */
export function formatScheduledAt(scheduledAt: string | null): string {
  return scheduledAt ? DATE_TIME.format(new Date(scheduledAt)) : 'Date à définir';
}

export function formatVenue(meeting: Pick<GeneralMeeting, 'venueType' | 'venueAddress' | 'venueLink'>): string {
  switch (meeting.venueType) {
    case 'PHYSICAL':
      return meeting.venueAddress ?? '—';
    case 'VIDEOCONFERENCE':
      return `Visioconférence : ${meeting.venueLink ?? '—'}`;
    case 'HYBRID':
      return `${meeting.venueAddress ?? '—'} (et en visioconférence)`;
    default:
      return 'Lieu à définir';
  }
}

/** Voices are tantièmes or plain lot counts depending on the property's mode - never money. */
export function formatWeight(weight: number): string {
  return weight.toLocaleString('fr-FR', { maximumFractionDigits: 2 });
}

export function formatLotLabel(unitNumber: string | null, buildingName: string | null): string {
  if (!unitNumber) {
    return '—';
  }
  return buildingName ? `${buildingName} — ${unitNumber}` : unitNumber;
}

/**
 * An ISO instant back into the local wall time `<input type="datetime-local">`
 * expects (`yyyy-MM-ddTHH:mm`, no zone). Built from the local getters rather
 * than by slicing toISOString(), which would show the UTC hour and quietly
 * shift the meeting by one or two hours in Casablanca.
 */
export function toDateTimeLocalValue(isoInstant: string | null): string {
  if (!isoInstant) {
    return '';
  }
  const date = new Date(isoInstant);
  const pad = (value: number) => String(value).padStart(2, '0');
  return `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())}T${pad(date.getHours())}:${pad(date.getMinutes())}`;
}
