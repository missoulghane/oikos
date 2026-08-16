import { httpClient } from '@/shared/api/httpClient';

/**
 * Downloads the convocation letter of one lot. Same shape as
 * downloadPaymentReceipt: the API answers with the PDF bytes, and the blob is
 * handed to the browser rather than opened in a tab - a syndic convoking by
 * post wants the file.
 */
export async function downloadConvocationDocument(convocationId: string, fileName: string): Promise<void> {
  const { data } = await httpClient.get(`/convocations/${convocationId}/document`, { responseType: 'blob' });

  const url = URL.createObjectURL(data as Blob);
  const link = document.createElement('a');
  link.href = url;
  link.download = fileName;
  link.click();
  URL.revokeObjectURL(url);
}
