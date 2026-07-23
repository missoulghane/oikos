import { httpClient } from '@/shared/api/httpClient';
import type { UnitTypePrice } from '@/features/property-mngt/pricing/types/pricing.types';

export interface SetUnitTypePriceParams {
  propertyId: string;
  unitTypeId: string;
  price: number;
}

export async function setUnitTypePrice({ propertyId, unitTypeId, price }: SetUnitTypePriceParams): Promise<UnitTypePrice> {
  const { data } = await httpClient.put<UnitTypePrice>(`/properties/${propertyId}/unit-type-prices/${unitTypeId}`, {
    price,
  });
  return data;
}
