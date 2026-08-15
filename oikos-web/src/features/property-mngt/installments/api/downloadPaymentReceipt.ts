import { httpClient } from '@/shared/api/httpClient';

/**
 * The receipt is served by its payment, not by its document id: the endpoint is
 * guarded by managesPayment (owner of the lot, or staff), whereas the generic
 * /documents/{id}/content route checks a property-wide permission every
 * copropriétaire holds - it would expose one owner's receipt to another.
 *
 * Same blob + object-URL dance as downloadDocument: the request needs the Bearer
 * header, so a plain <a href> cannot fetch it.
 */
export async function downloadPaymentReceipt(paymentId: string, fileName: string): Promise<void> {
  const { data } = await httpClient.get(`/payments/${paymentId}/receipt`, { responseType: 'blob' });

  const url = URL.createObjectURL(data as Blob);
  const link = document.createElement('a');
  link.href = url;
  link.download = fileName;
  link.click();
  URL.revokeObjectURL(url);
}
