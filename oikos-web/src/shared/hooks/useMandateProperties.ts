import { useProperties } from '@/features/property-mngt/properties/hooks/useProperties';
import type { Property } from '@/features/property-mngt/properties/types/property.types';

/**
 * Resolves display names for a small set of board-mandate property ids (a
 * handful at most - see SpaceSwitcher's own note on why this is not meant
 * for a professional manager's whole portfolio). GET /properties already
 * scopes to "properties this account has a staff role on", so no dedicated
 * endpoint is needed - this just fetches it once and indexes by id.
 */
export function useMandateProperties(mandateIds: string[]) {
  const properties = useProperties(0, Math.max(mandateIds.length, 5));
  const byId = new Map<string, Property>(properties.data?.content.map((property) => [property.id, property]) ?? []);
  return { isLoading: properties.isLoading, byId };
}
