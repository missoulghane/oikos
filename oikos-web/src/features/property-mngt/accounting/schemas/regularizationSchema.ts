import { z } from 'zod';

// Mirrors the bean validation constraints on RecordUnitAccountRegularizationRequest (oikos-api).
export const regularizationSchema = z.object({
  amount: z.number({ error: 'Le montant est requis' }).positive('Doit être supérieur à 0'),
  direction: z.enum(['DEBIT', 'CREDIT'], { error: 'Le sens est requis' }),
  label: z.string().min(1, 'Le libellé est requis'),
  reason: z.string().min(1, 'Le motif est requis'),
});

export type RegularizationFormValues = z.infer<typeof regularizationSchema>;
