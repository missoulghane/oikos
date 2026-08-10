import { z } from 'zod';

// Mirrors the bean validation constraints on RecordOwnerPaymentRequest (oikos-api).
export const recordOwnerPaymentSchema = z.object({
  mode: z.enum(['BANK_TRANSFER', 'CASH', 'CHECK', 'DIRECT_DEBIT'], { error: 'Le moyen de paiement est requis' }),
  treasuryAccountId: z.string().min(1, 'Le compte impacté est requis'),
  valueDate: z.string().min(1, 'La date est requise'),
  amount: z.number({ error: 'Le montant est requis' }).positive('Doit être supérieur à 0'),
});

export type RecordOwnerPaymentFormValues = z.infer<typeof recordOwnerPaymentSchema>;
