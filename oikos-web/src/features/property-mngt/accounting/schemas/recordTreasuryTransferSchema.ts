import { z } from 'zod';

// Mirrors the bean validation constraints on RecordTreasuryTransferRequest (oikos-api);
// the account-equality check mirrors IdenticalTreasuryTransferAccountsException.
export const recordTreasuryTransferSchema = z
  .object({
    sourceAccountId: z.string().min(1, 'Le compte source est requis'),
    destinationAccountId: z.string().min(1, 'Le compte destination est requis'),
    pieceDate: z.string().min(1, 'La date est requise'),
    amount: z.number({ error: 'Le montant est requis' }).positive('Doit être supérieur à 0'),
    description: z.string().trim().max(1000, '1000 caractères maximum').optional(),
  })
  .refine((values) => values.sourceAccountId === '' || values.sourceAccountId !== values.destinationAccountId, {
    message: 'Les comptes source et destination doivent être différents',
    path: ['destinationAccountId'],
  });

export type RecordTreasuryTransferFormValues = z.infer<typeof recordTreasuryTransferSchema>;
