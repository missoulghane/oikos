import { z } from 'zod';

// Mirrors the bean validation constraints on RecordSupplierPaymentRequest (oikos-api).
export const recordSupplierPaymentSchema = z.object({
  ledgerAccountId: z.string().min(1, 'Le compte de charge est requis'),
  treasuryAccountId: z.string().min(1, 'Le compte impacté est requis'),
  pieceDate: z.string().min(1, 'La date est requise'),
  amount: z.number({ error: 'Le montant est requis' }).positive('Doit être supérieur à 0'),
  description: z.string().trim().max(1000, '1000 caractères maximum').optional(),
  externalReference: z.string().trim().max(200, '200 caractères maximum').optional(),
});

export type RecordSupplierPaymentFormValues = z.infer<typeof recordSupplierPaymentSchema>;
