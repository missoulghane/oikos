import { z } from 'zod';

// Mirrors the bean validation constraints on CreateFinancialAccountRequest (oikos-api).
export const createFinancialAccountSchema = z.object({
  name: z.string().min(1, 'Le nom est requis').max(200, 'Maximum 200 caractères'),
  type: z.enum(['CASH', 'BANK', 'MOBILE_MONEY'], { error: 'Le type est requis' }),
  currency: z.string().min(1, 'La devise est requise').max(10, 'Maximum 10 caractères'),
});

export type CreateFinancialAccountFormValues = z.infer<typeof createFinancialAccountSchema>;
