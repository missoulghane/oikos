const relativeTimeFormat = new Intl.RelativeTimeFormat('fr', { numeric: 'auto' });

const MINUTE_MS = 60_000;
const HOUR_MS = 60 * MINUTE_MS;
const DAY_MS = 24 * HOUR_MS;
const WEEK_MS = 7 * DAY_MS;

/** Short relative label for a recent timestamp (e.g. "il y a 5 minutes", "hier") - shared by
 * messaging (message/conversation timestamps) and notifications (notification timestamps). */
export function formatRelativeTime(isoInstant: string): string {
  const date = new Date(isoInstant);
  const diffMs = date.getTime() - Date.now();
  const absMs = Math.abs(diffMs);

  if (absMs < MINUTE_MS) {
    return "à l'instant";
  }
  if (absMs < HOUR_MS) {
    return relativeTimeFormat.format(Math.round(diffMs / MINUTE_MS), 'minute');
  }
  if (absMs < DAY_MS) {
    return relativeTimeFormat.format(Math.round(diffMs / HOUR_MS), 'hour');
  }
  if (absMs < WEEK_MS) {
    return relativeTimeFormat.format(Math.round(diffMs / DAY_MS), 'day');
  }
  return date.toLocaleDateString('fr-FR');
}
