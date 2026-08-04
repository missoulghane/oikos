import { z } from 'zod';

// Mirrors the bean validation constraint on SetProjectedBudgetRequest (oikos-api).
export const projectedBudgetSchema = z.object({
  projectedBudget: z.number({ error: 'Le budget prévisionnel est requis' }).positive('Doit être supérieur à 0'),
});

export type ProjectedBudgetFormValues = z.infer<typeof projectedBudgetSchema>;
