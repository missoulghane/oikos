import { describe, expect, it } from 'vitest';
import {
  agendaItemSchema,
  createGeneralMeetingSchema,
} from '@/features/property-mngt/general-meetings/schemas/generalMeetingSchemas';

describe('createGeneralMeetingSchema', () => {
  const complete = {
    meetingType: 'ORDINARY' as const,
    title: 'AG 2026',
    scheduledAt: '2026-09-15T17:00',
    venueType: 'PHYSICAL' as const,
    venueAddress: '12 rue des Orangers',
  };

  it('accepts a complete creation', () => {
    expect(createGeneralMeetingSchema.safeParse(complete).success).toBe(true);
  });

  it('requires an intitulé', () => {
    expect(createGeneralMeetingSchema.safeParse({ ...complete, title: '   ' }).success).toBe(false);
  });

  it('requires a date - it is shown at the top of every tab afterwards', () => {
    expect(createGeneralMeetingSchema.safeParse({ ...complete, scheduledAt: '' }).success).toBe(false);
  });

  it('applies the same venue rule as scheduling', () => {
    const noAddress = createGeneralMeetingSchema.safeParse({ ...complete, venueAddress: undefined });
    expect(noAddress.success).toBe(false);
    expect(noAddress.error?.issues[0]?.path).toEqual(['venueAddress']);

    expect(
      createGeneralMeetingSchema.safeParse({
        ...complete,
        venueType: 'VIDEOCONFERENCE',
        venueAddress: undefined,
        venueLink: 'https://meet.example/ag',
      }).success,
    ).toBe(true);
  });
});

describe('agendaItemSchema', () => {
  it('requires a majority rule - a point nobody assigned one to cannot be decided', () => {
    expect(agendaItemSchema.safeParse({ label: 'Travaux' }).success).toBe(false);
  });

  it('accepts a point without description', () => {
    expect(agendaItemSchema.safeParse({ label: 'Travaux', majorityRule: 'ABSOLUTE' }).success).toBe(true);
  });
});
