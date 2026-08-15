import { useMutation } from '@tanstack/react-query';
import { downloadPaymentReceipt } from '@/features/property-mngt/installments/api/downloadPaymentReceipt';

export function useDownloadPaymentReceipt() {
  return useMutation({
    mutationFn: ({ paymentId, fileName }: { paymentId: string; fileName: string }) =>
      downloadPaymentReceipt(paymentId, fileName),
  });
}
