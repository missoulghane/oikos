import { z } from 'zod';
import { DUES_CALCULATION_MODES } from '@/features/property-mngt/properties/types/property.types';

// Mirrors the bean validation constraint on UpdateDuesCalculationModeRequest (oikos-api).
export const duesCalculationModeSchema = z.object({
  mode: z.enum(DUES_CALCULATION_MODES, { error: 'Le mode est requis' }),
});

export type DuesCalculationModeFormValues = z.infer<typeof duesCalculationModeSchema>;
