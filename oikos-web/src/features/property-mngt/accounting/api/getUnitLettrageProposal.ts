import { httpClient } from '@/shared/api/httpClient';
import type { LettrageProposal } from '@/features/property-mngt/accounting/types/accounting.types';

export async function getUnitLettrageProposal(propertyId: string, unitId: string): Promise<LettrageProposal> {
  const { data } = await httpClient.get<LettrageProposal>(
    `/properties/${propertyId}/accounting/units/${unitId}/lettrage-proposal`,
  );
  return data;
}
