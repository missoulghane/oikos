import { httpClient } from '@/shared/api/httpClient';
import type {
  ChannelCode,
  Convocation,
  DeliveryStatus,
} from '@/features/property-mngt/general-meetings/types/convocation.types';

/**
 * Records that a person delivered it - by post, by registered mail or by hand.
 * Appends an attempt; it never replaces the ones already there.
 */
export async function recordConvocationDelivery(
  convocationId: string,
  channel: ChannelCode,
  deliveryStatus: DeliveryStatus,
  reference?: string,
): Promise<Convocation> {
  const { data } = await httpClient.put<Convocation>(`/convocations/${convocationId}/delivery-status`, {
    channel,
    deliveryStatus,
    reference: reference?.trim() ? reference.trim() : null,
  });
  return data;
}
