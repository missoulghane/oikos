import { z } from 'zod';

// Mirrors the bean validation constraints on RecordPayrollExpenseRequest (oikos-api).
export const recordPayrollExpenseSchema = z.object({
  date: z.string().min(1, 'La date est requise'),
  ledgerAccountId: z.string().min(1, 'Le compte de charge est requis'),
  amount: z.number({ error: 'Le montant est requis' }).positive('Doit être supérieur à 0'),
  description: z.string().trim().max(1000, '1000 caractères maximum').optional(),
});

export type RecordPayrollExpenseFormValues = z.infer<typeof recordPayrollExpenseSchema>;
