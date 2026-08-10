import { z } from 'zod';

export const closePeriodSchema = z.object({
  period: z.string().regex(/^\d{4}-\d{2}$/, 'La période doit être un mois (yyyy-MM)'),
});

export type ClosePeriodFormValues = z.infer<typeof closePeriodSchema>;
