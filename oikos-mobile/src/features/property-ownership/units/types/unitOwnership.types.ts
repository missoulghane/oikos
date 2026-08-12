// Mirrors the subset of property-mngt/properties/types/property.types.ts
// (oikos-web) needed to list a unit's co-owners read-only - property-mngt
// doesn't exist on mobile (out of scope, see PLAN.md), so this is
// co-located with the feature that needs it instead.
export const PARTY_TYPES = ['INDIVIDUAL', 'COMPANY'] as const;
export type PartyType = (typeof PARTY_TYPES)[number];

export const PARTY_TYPE_LABELS: Record<PartyType, string> = {
  INDIVIDUAL: 'Particulier',
  COMPANY: 'Société',
};

export interface UnitOwnership {
  id: string;
  unitId: string;
  partyId: string;
  partyFullName: string;
  partyType: PartyType;
  partyEmail: string;
  ownershipShare: number;
}
