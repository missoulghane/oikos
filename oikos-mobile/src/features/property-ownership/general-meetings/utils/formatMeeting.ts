import type { MyConvocation } from '@/features/property-ownership/general-meetings/types/generalMeeting.types';

const DATE_TIME = new Intl.DateTimeFormat('fr-FR', {
  dateStyle: 'long',
  timeStyle: 'short',
  timeZone: 'Africa/Casablanca',
});

export function formatScheduledAt(scheduledAt: string | null): string {
  return scheduledAt ? DATE_TIME.format(new Date(scheduledAt)) : 'Date à définir';
}

export function formatVenue(
  venue: Pick<MyConvocation, 'venueType' | 'venueAddress' | 'venueLink'>,
): string {
  switch (venue.venueType) {
    case 'PHYSICAL':
      return venue.venueAddress ?? '—';
    case 'VIDEOCONFERENCE':
      return `Visioconférence : ${venue.venueLink ?? '—'}`;
    case 'HYBRID':
      return `${venue.venueAddress ?? '—'} (et en visioconférence)`;
    default:
      return 'Lieu à définir';
  }
}

export function formatLotLabel(unitNumber: string | null, buildingName: string | null): string {
  if (!unitNumber) {
    return '—';
  }
  return buildingName ? `${buildingName} — ${unitNumber}` : unitNumber;
}

export function formatWeight(weight: number): string {
  return weight.toLocaleString('fr-FR', { maximumFractionDigits: 2 });
}
