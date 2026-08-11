import { z } from 'zod';

// Mirrors the bean validation constraints on AddBankAccountRequest (oikos-api).
// bankAccountNumber is free text (RIB, IBAN, foreign formats...) and optional:
// the account can be declared now and its details filled in later.
export const addBankAccountSchema = z.object({
  label: z.string().trim().min(1, 'Le libellé est requis').max(200, '200 caractères maximum'),
  bankAccountNumber: z.string().trim().max(64, '64 caractères maximum').optional().or(z.literal('')),
});

export type AddBankAccountFormValues = z.infer<typeof addBankAccountSchema>;
