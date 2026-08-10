import { z } from 'zod';

// Mirrors the bean validation constraints on OpenAccountingExerciseRequest (oikos-api).
export const openExerciseSchema = z.object({
  label: z.string().trim().min(1, 'Le libellé est requis').max(200, '200 caractères maximum'),
  startDate: z.string().min(1, 'La date de début est requise'),
  endDate: z.string().min(1, 'La date de fin est requise'),
  comment: z.string().trim().max(1000, '1000 caractères maximum').optional(),
});

export type OpenExerciseFormValues = z.infer<typeof openExerciseSchema>;
