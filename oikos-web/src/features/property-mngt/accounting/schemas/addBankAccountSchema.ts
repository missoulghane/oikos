import { z } from 'zod';

// Mirrors the bean validation constraints on AddBankAccountRequest (oikos-api).
export const addBankAccountSchema = z.object({
  label: z.string().trim().min(1, 'Le libellé est requis').max(200, '200 caractères maximum'),
});

export type AddBankAccountFormValues = z.infer<typeof addBankAccountSchema>;
