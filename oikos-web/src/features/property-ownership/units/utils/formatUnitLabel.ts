import type { OwnedUnit } from '@/features/property-ownership/units/types/unit.types';

/**
 * The canonical way an owner's lot is named across the app - it was inlined
 * identically in MyUnitsPage, MyInstallmentsPage, MyPaymentsPage and their
 * mobile twins, so the installments/payments tables reuse this instead of
 * spelling the template out a fifth time.
 */
export function formatUnitLabel(unit: OwnedUnit): string {
  return `${unit.propertyName} — ${unit.buildingName} — ${unit.unitNumber}`;
}
