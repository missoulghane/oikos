import { httpClient } from '@/shared/api/httpClient';

export interface RemoveUnitTypePriceParams {
  propertyId: string;
  unitTypeId: string;
}

export async function removeUnitTypePrice({ propertyId, unitTypeId }: RemoveUnitTypePriceParams): Promise<void> {
  await httpClient.delete(`/properties/${propertyId}/unit-type-prices/${unitTypeId}`);
}
