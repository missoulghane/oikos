/**
 * Mobile has no date-picker dependency, and two free date fields are poor UX on
 * a phone anyway - filters offer preset ranges instead, resolved here to the
 * same "YYYY-MM-DD" bounds the pure filter functions already take (so the
 * filtering logic stays identical to oikos-web's).
 */
export type DateRangePreset = '' | 'LAST_3_MONTHS' | 'LAST_12_MONTHS' | 'THIS_YEAR';

export const DATE_RANGE_PRESET_LABELS: Record<DateRangePreset, string> = {
  '': 'Toutes',
  LAST_3_MONTHS: '3 derniers mois',
  LAST_12_MONTHS: '12 derniers mois',
  THIS_YEAR: 'Cette année',
};

function toIsoDate(date: Date): string {
  // Built from the local calendar fields rather than toISOString(), which would
  // shift the day backwards for timezones east of UTC.
  const month = `${date.getMonth() + 1}`.padStart(2, '0');
  const day = `${date.getDate()}`.padStart(2, '0');
  return `${date.getFullYear()}-${month}-${day}`;
}

/** Lower bound of the preset, or '' for "no filter". Today is always the upper bound. */
export function presetStartDate(preset: DateRangePreset, today: Date = new Date()): string {
  if (preset === '') {
    return '';
  }
  if (preset === 'THIS_YEAR') {
    return `${today.getFullYear()}-01-01`;
  }
  const months = preset === 'LAST_3_MONTHS' ? 3 : 12;
  const start = new Date(today.getFullYear(), today.getMonth() - months, today.getDate());
  return toIsoDate(start);
}
