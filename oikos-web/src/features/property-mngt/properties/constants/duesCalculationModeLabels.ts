import type { DuesCalculationMode } from '@/features/property-mngt/properties/types/property.types';

export const DUES_CALCULATION_MODE_LABELS: Record<DuesCalculationMode, string> = {
  FLAT_RATE: 'Forfait',
  SHARES: 'Tantième',
};
