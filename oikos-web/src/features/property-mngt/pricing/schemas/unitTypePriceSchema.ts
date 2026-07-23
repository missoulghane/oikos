import { z } from 'zod';

// Mirrors the bean validation constraint on SetUnitTypePriceRequest (oikos-api).
export const unitTypePriceSchema = z.object({
  price: z.number({ error: 'Le prix est requis' }).min(0, 'Ne peut pas être négatif'),
});

export type UnitTypePriceFormValues = z.infer<typeof unitTypePriceSchema>;
