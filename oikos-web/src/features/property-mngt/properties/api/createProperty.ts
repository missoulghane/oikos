import { httpClient } from '@/shared/api/httpClient';
import type { CreatePropertyPayload } from '@/features/property-mngt/properties/types/property.types';

export async function createProperty(payload: CreatePropertyPayload): Promise<void> {
  await httpClient.post('/properties', payload);
}
