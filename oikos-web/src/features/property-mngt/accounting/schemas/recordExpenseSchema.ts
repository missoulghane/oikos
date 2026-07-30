import { z } from 'zod';

// Mirrors the bean validation constraints on RecordExpenseRequest (oikos-api).
export const recordExpenseSchema = z.object({
  financialAccountId: z.string().min(1, 'Le compte est requis'),
  date: z.string().min(1, 'La date est requise'),
  category: z.string().min(1, 'La catégorie est requise').max(100, 'Maximum 100 caractères'),
  provider: z.string().min(1, 'Le fournisseur est requis').max(200, 'Maximum 200 caractères'),
  amount: z.number({ error: 'Le montant est requis' }).positive('Doit être supérieur à 0'),
  description: z.string().max(1000, 'Maximum 1000 caractères').optional(),
  receiptReference: z.string().max(200, 'Maximum 200 caractères').optional(),
});

export type RecordExpenseFormValues = z.infer<typeof recordExpenseSchema>;
