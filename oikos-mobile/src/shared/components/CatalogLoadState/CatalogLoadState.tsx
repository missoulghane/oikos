import { StyleSheet, View } from 'react-native';
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
 * Même composant que oikos-web (shared/components/CatalogLoadState) : l'attente
 * d'une liste de paramétrage, ou son échec avec de quoi relancer - sans quoi un
 * appel tombé laissait l'écran sans liste et sans explication.
 */
export function CatalogLoadState({ query, loadingLabel, errorMessage }: CatalogLoadStateProps) {
  if (!query.isError) {
    return <Loader label={loadingLabel} />;
  }
  return (
    <View style={styles.container}>
      <Alert message={errorMessage} />
      <Button variant="secondary" isLoading={query.isFetching} onPress={() => query.refetch()}>
        Réessayer
      </Button>
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    gap: 8,
  },
});
