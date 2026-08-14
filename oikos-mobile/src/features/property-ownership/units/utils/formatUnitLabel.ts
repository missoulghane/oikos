import type { OwnedUnit } from '@/features/property-ownership/units/types/unit.types';

/** Canonical lot label, mirrors oikos-web's shared/../units/utils/formatUnitLabel.ts. */
export function formatUnitLabel(unit: OwnedUnit): string {
  return `${unit.propertyName} — ${unit.buildingName} — Lot ${unit.unitNumber}`;
}
