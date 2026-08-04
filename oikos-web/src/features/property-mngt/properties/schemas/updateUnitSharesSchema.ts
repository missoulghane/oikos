import { z } from 'zod';

// Mirrors the bean validation constraints on UpdateUnitSharesRequest (oikos-api).
export const updateUnitSharesSchema = z.object({
  shares: z
    .number({ error: 'Les tantièmes sont requis' })
    .min(0, 'Ne peut pas être négatif'),
});

export type UpdateUnitSharesFormValues = z.infer<typeof updateUnitSharesSchema>;
