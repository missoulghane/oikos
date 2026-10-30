import { z } from 'zod';
import { addUnitSchema } from '@/features/property-mngt/properties/schemas/addUnitSchema';

/**
 * Mirrors UpdateUnitRequest (oikos-api): the add form's rules for the number,
 * the floor and the shares - the type is not editable. Built per building, for
 * the floor bound, as addUnitSchema.
 */
export function updateUnitSchema(buildingFloorCount: number) {
  return addUnitSchema(buildingFloorCount).pick({ unitNumber: true, floor: true, shares: true });
}

export type UpdateUnitFormValues = z.infer<ReturnType<typeof updateUnitSchema>>;
