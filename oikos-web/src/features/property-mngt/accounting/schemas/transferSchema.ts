import { z } from 'zod';

// Mirrors the bean validation constraints on TransferBetweenFinancialAccountsRequest (oikos-api).
export const transferSchema = z
  .object({
    fromAccountId: z.string().min(1, 'Le compte source est requis'),
    toAccountId: z.string().min(1, 'Le compte destination est requis'),
    amount: z.number({ error: 'Le montant est requis' }).positive('Doit être supérieur à 0'),
    date: z.string().min(1, 'La date est requise'),
    label: z.string().min(1, 'Le libellé est requis'),
  })
  .refine((values) => values.fromAccountId !== values.toAccountId, {
    message: 'Les comptes source et destination doivent être différents',
    path: ['toAccountId'],
  });

export type TransferFormValues = z.infer<typeof transferSchema>;
