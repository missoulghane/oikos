import { z } from 'zod';

// Mirrors the bean validation constraints on RecordExceptionalDepositRequest (oikos-api).
export const recordExceptionalDepositSchema = z.object({
  financialAccountId: z.string().min(1, 'Le compte est requis'),
  amount: z.number({ error: 'Le montant est requis' }).positive('Doit être supérieur à 0'),
  date: z.string().min(1, 'La date est requise'),
  label: z.string().min(1, 'Le libellé est requis').max(200, 'Maximum 200 caractères'),
});

export type RecordExceptionalDepositFormValues = z.infer<typeof recordExceptionalDepositSchema>;
