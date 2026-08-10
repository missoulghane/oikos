import { Link } from 'react-router-dom';

export function ForbiddenPage() {
  return (
    <div className="flex min-h-screen flex-col items-center justify-center gap-3 px-4 text-center">
      <h1 className="text-2xl font-semibold text-gray-900 dark:text-white/90">403</h1>
      <p className="text-sm text-gray-500 dark:text-gray-400">Vous n'avez pas les droits nécessaires pour accéder à cette page.</p>
      <Link to="/" className="text-sm font-medium text-gray-900 dark:text-white/90 underline">
        Retour à l'accueil
      </Link>
    </div>
  );
}
