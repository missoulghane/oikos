import { httpClient } from '@/shared/api/httpClient';

/**
 * @param onProgress pourcentage reçu (0-100) ; non appelé quand la taille totale
 * est inconnue - l'API envoie toujours un Content-Length, mais un proxy peut le retirer.
 */
export async function downloadDocument(
  documentId: string,
  fileName: string,
  onProgress?: (percent: number) => void,
): Promise<void> {
  const { data } = await httpClient.get(`/documents/${documentId}/content`, {
    responseType: 'blob',
    onDownloadProgress: onProgress
      ? (event) => {
          if (event.total) {
            onProgress((event.loaded / event.total) * 100);
          }
        }
      : undefined,
  });

  const url = URL.createObjectURL(data as Blob);
  const link = document.createElement('a');
  link.href = url;
  link.download = fileName;
  link.click();
  URL.revokeObjectURL(url);
}
