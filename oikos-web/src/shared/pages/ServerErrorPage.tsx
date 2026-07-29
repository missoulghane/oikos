export function ServerErrorPage() {
  return (
    <div className="flex min-h-screen flex-col items-center justify-center gap-3 px-4 text-center">
      <h1 className="text-2xl font-semibold text-gray-900">500</h1>
      <p className="text-sm text-gray-500">Le serveur a rencontré une erreur. Merci de réessayer plus tard.</p>
    </div>
  );
}
