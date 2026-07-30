import { z } from 'zod';

// Mirrors the bean validation constraints on OpenAccountingExerciseRequest (oikos-api).
export const openExerciseSchema = z.object({
  label: z.string().min(1, 'Le libellé est requis').max(200, 'Maximum 200 caractères'),
  startDate: z.string().min(1, 'La date de début est requise'),
  endDate: z.string().min(1, 'La date de fin est requise'),
  comment: z.string().max(1000, 'Maximum 1000 caractères').optional(),
});

export type OpenExerciseFormValues = z.infer<typeof openExerciseSchema>;
