import { describe, expect, it } from 'vitest';
import { toDateTimeLocalValue } from '@/features/property-mngt/general-meetings/utils/formatMeeting';

describe('toDateTimeLocalValue', () => {
  it('is empty for a draft with no date yet', () => {
    expect(toDateTimeLocalValue(null)).toBe('');
  });

  it('round-trips through the same local wall time the input produced', () => {
    // What the scheduling form does: read the stored instant back into the field.
    // Slicing toISOString() instead would print the UTC hour and shift the meeting.
    const local = '2026-09-15T18:00';
    const instant = new Date(local).toISOString();

    expect(toDateTimeLocalValue(instant)).toBe(local);
  });

  it('pads single-digit months, days and hours', () => {
    expect(toDateTimeLocalValue(new Date('2026-01-05T09:07').toISOString())).toBe('2026-01-05T09:07');
  });
});
