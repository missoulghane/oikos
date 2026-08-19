import { isRouteErrorResponse, Link, useRouteError } from 'react-router-dom';
import { BrandMark } from '@/shared/components/BrandLogo/BrandLogo';
import { Button } from '@/shared/components/Button/Button';
import { ForbiddenPage } from '@/shared/pages/ForbiddenPage';
import { NotFoundPage } from '@/shared/pages/NotFoundPage';

/**
 * Ce qui s'affiche quand une page tombe en cours de rendu, à la place de l'écran
 * blanc que laissait React jusqu'ici.
 *
 * <p>Deux issues sont offertes, et l'ordre compte : « Recharger » d'abord, parce
 * qu'une panne de rendu est le plus souvent un état de page corrompu qu'un
 * rechargement remet d'aplomb ; l'accueil ensuite, pour celui qui vient de la
 * réessayer sans succès.
 *
 * <p>Le détail technique n'est déplié qu'en développement. En production il ne
 * dirait rien d'actionnable au syndic qui le lit, et une trace d'exécution
 * nomme volontiers des fichiers et des données qui ne le regardent pas.
 */
export function AppErrorPage() {
  const error = useRouteError();

  // Un 404/403 remonté par le routeur garde sa page dédiée : « une erreur s'est
  // produite » serait une réponse fausse à une question précise.
  if (isRouteErrorResponse(error)) {
    if (error.status === 404) {
      return <NotFoundPage />;
    }
    if (error.status === 403) {
      return <ForbiddenPage />;
    }
  }

  const detail = error instanceof Error ? (error.stack ?? error.message) : String(error);

  return (
    <div className="flex min-h-screen flex-col items-center justify-center gap-4 px-4 py-10 text-center">
      <BrandMark size="lg" />
      <h1 className="text-2xl font-semibold text-gray-900 dark:text-white/90">Une erreur s'est produite</h1>
      <p className="max-w-md text-sm text-gray-500 dark:text-gray-400">
        Cette page n'a pas pu s'afficher. Rechargez-la pour reprendre où vous en étiez ; si l'erreur revient,
        revenez à l'accueil et signalez-la nous.
      </p>
      <div className="mt-2 flex flex-wrap items-center justify-center gap-3">
        {/* Un rechargement complet, et non un re-rendu : c'est justement l'état
            en mémoire qui vient d'échouer, le reconstruire est tout l'intérêt. */}
        <Button onClick={() => window.location.reload()}>Recharger la page</Button>
        <Link
          to="/"
          className="inline-flex min-h-11 items-center justify-center rounded-lg bg-white px-4 py-2 text-sm font-medium text-gray-700 shadow-theme-xs ring-1 ring-inset ring-gray-300 hover:bg-gray-50 dark:bg-gray-800 dark:text-gray-400 dark:ring-gray-700 dark:hover:bg-white/[0.03]"
        >
          Retour à l'accueil
        </Link>
      </div>
      {import.meta.env.DEV && (
        <details className="mt-4 w-full max-w-2xl text-left">
          <summary className="cursor-pointer text-sm text-gray-500 dark:text-gray-400">Détail technique</summary>
          <pre className="mt-2 overflow-x-auto rounded-lg bg-gray-50 p-3 text-xs text-gray-700 dark:bg-white/[0.03] dark:text-gray-300">
            {detail}
          </pre>
        </details>
      )}
    </div>
  );
}
