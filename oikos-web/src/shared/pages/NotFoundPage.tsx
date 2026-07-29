import { Link } from 'react-router-dom';

export function NotFoundPage() {
  return (
    <div className="flex min-h-screen flex-col items-center justify-center gap-3 px-4 text-center">
      <h1 className="text-2xl font-semibold text-gray-900">404</h1>
      <p className="text-sm text-gray-500">Cette page n'existe pas.</p>
      <Link to="/" className="text-sm font-medium text-gray-900 underline">
        Retour à l'accueil
      </Link>
    </div>
  );
}
