import { z } from 'zod';

// Mirrors the bean validation constraints of oikos-api's request records.
//
// The API accepts a dateless draft; this form does not. A date and a place are
// what the syndic actually decides when opening an AG, and they are shown at
// the top of every one of its tabs afterwards - creating without them would
// only mean coming back to fill them in.
export const createGeneralMeetingSchema = z
  .object({
    meetingType: z.enum(['ORDINARY', 'EXTRAORDINARY']),
    title: z.string().trim().min(1, 'Un intitulé est requis').max(200, 'Intitulé limité à 200 caractères'),
    scheduledAt: z.string().min(1, 'Une date et une heure sont requises'),
    venueType: z.enum(['PHYSICAL', 'VIDEOCONFERENCE', 'HYBRID']),
    venueAddress: z.string().trim().max(250, 'Adresse limitée à 250 caractères').optional(),
    venueLink: z.string().trim().max(500, 'Lien limité à 500 caractères').optional(),
  })
  .refine((values) => values.venueType === 'VIDEOCONFERENCE' || Boolean(values.venueAddress), {
    path: ['venueAddress'],
    message: 'Une adresse est requise pour une séance sur place',
  })
  .refine((values) => values.venueType === 'PHYSICAL' || Boolean(values.venueLink), {
    path: ['venueLink'],
    message: 'Un lien de connexion est requis pour une séance à distance',
  });

export type CreateGeneralMeetingFormValues = z.infer<typeof createGeneralMeetingSchema>;

export const agendaItemSchema = z.object({
  label: z.string().trim().min(1, 'Un libellé est requis').max(200, 'Libellé limité à 200 caractères'),
  // 20 000, like a meeting's comment: the column is `text`, the 4 000 that stood here came
  // from nothing but caution, and a point of an agenda carries the resolution as it will be
  // put to the vote - a full paragraph of contract terms, not a note.
  description: z.string().trim().max(20000, 'Description limitée à 20 000 caractères').optional(),
  majorityRule: z.enum(['SIMPLE', 'ABSOLUTE', 'UNANIMITY']),
});

export type AgendaItemFormValues = z.infer<typeof agendaItemSchema>;

export const quorumSettingSchema = z.object({
  meetingType: z.enum(['ORDINARY', 'EXTRAORDINARY']),
  // Plain number rather than z.coerce: the field is registered with
  // valueAsNumber, so the form already hands over a number and coercion would
  // make the schema's input and output types diverge (react-hook-form rejects
  // the resulting resolver).
  quorumPercentage: z
    .number({ message: 'Un pourcentage est requis' })
    .min(0, 'Le quorum ne peut pas être négatif')
    .max(100, 'Le quorum ne peut pas dépasser 100 %'),
});

export type QuorumSettingFormValues = z.infer<typeof quorumSettingSchema>;
