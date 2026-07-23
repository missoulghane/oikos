type AlertVariant = 'error' | 'success';

const VARIANT_CLASSES: Record<AlertVariant, string> = {
  error: 'border-red-200 bg-red-50 text-red-700',
  success: 'border-green-200 bg-green-50 text-green-700',
};

export function Alert({ message, variant = 'error' }: { message: string; variant?: AlertVariant }) {
  return (
    <div role="alert" className={`rounded-md border px-3 py-2 text-sm ${VARIANT_CLASSES[variant]}`}>
      {message}
    </div>
  );
}
