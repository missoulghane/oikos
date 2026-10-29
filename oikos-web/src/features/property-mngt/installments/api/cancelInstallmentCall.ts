import { httpClient } from '@/shared/api/httpClient';

/** Contre-passe l'écriture de l'appel et le passe au statut « Annulé » - un appel comptabilisé n'est jamais supprimé. */
export async function cancelInstallmentCall(installmentCallId: string): Promise<void> {
  await httpClient.post(`/installment-calls/${installmentCallId}/cancellation`);
}
