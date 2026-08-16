import { httpClient } from '@/shared/api/httpClient';
import type { MyConvocation } from '@/features/property-ownership/general-meetings/types/myConvocation.types';

export async function listMyConvocations(): Promise<MyConvocation[]> {
  const { data } = await httpClient.get<MyConvocation[]>('/users/me/convocations');
  return data;
}
