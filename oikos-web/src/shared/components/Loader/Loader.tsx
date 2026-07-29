export function Loader({ label = 'Chargement…' }: { label?: string }) {
  return (
    <div role="status" className="flex items-center justify-center gap-2 py-8 text-gray-500">
      <span className="h-5 w-5 animate-spin rounded-full border-2 border-gray-200 border-t-brand-500" />
      <span className="text-sm">{label}</span>
    </div>
  );
}
