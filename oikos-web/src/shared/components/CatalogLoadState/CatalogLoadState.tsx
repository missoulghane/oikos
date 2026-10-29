import { Alert } from '@/shared/components/Alert/Alert';
import { Button } from '@/shared/components/Button/Button';
import { Loader } from '@/shared/components/Loader/Loader';

interface CatalogLoadStateProps {
  /** Le résultat useQuery d'un catalogue (rôles, types de lot), réduit à ce qui sert ici. */
  query: { isError: boolean; isFetching: boolean; refetch: () => unknown };
  loadingLabel: string;
  errorMessage: string;
}

/**
 * Ce qui s'affiche à la place d'une liste de paramétrage pas encore reçue :
 * l'attente, ou l'échec avec de quoi relancer. Sans la seconde branche, un appel
 * tombé (API en redémarrage juste après un déploiement) laissait l'écran sur
 * « Chargement… » pour toujours, puisque rien ne relançait la requête.
 */
export function CatalogLoadState({ query, loadingLabel, errorMessage }: CatalogLoadStateProps) {
  if (!query.isError) {
    return <Loader label={loadingLabel} />;
  }
  return (
    <div className="flex flex-col gap-2">
      <Alert message={errorMessage} />
      <Button
        type="button"
        variant="secondary"
        className="w-fit"
        isLoading={query.isFetching}
        onClick={() => query.refetch()}
      >
        Réessayer
      </Button>
    </div>
  );
}
