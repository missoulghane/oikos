import { FlatList, StyleSheet, Text, View } from 'react-native';
import { SafeAreaView } from 'react-native-safe-area-context';
import type { NativeStackScreenProps } from '@react-navigation/native-stack';
import { useMyUnits } from '@/features/property-ownership/units/hooks/useMyUnits';
import { useMyInstallments } from '@/features/property-ownership/installments/hooks/useMyInstallments';
import { MyUnitCard } from '@/features/property-ownership/units/components/MyUnitCard';
import { unitOutstanding } from '@/features/property-ownership/units/utils/unitBalance';
import { Button } from '@/shared/components/Button/Button';
import { Card } from '@/shared/components/Card/Card';
import { Loader } from '@/shared/components/Loader/Loader';
import { Alert } from '@/shared/components/Alert/Alert';
import { EmptyState } from '@/shared/components/EmptyState/EmptyState';
import { getErrorMessage } from '@/shared/utils/getErrorMessage';
import { colors } from '@/shared/theme/colors';
import type { HomeStackParamList } from '@/app/navigation/HomeStackNavigator';
import type { OwnedUnit } from '@/features/property-ownership/units/types/unit.types';

type Props = NativeStackScreenProps<HomeStackParamList, 'Home'>;

export function MyUnitsScreen({ navigation }: Props) {
  const units = useMyUnits();
  // One request for every lot's echeances rather than one per card - the owner
  // endpoint already returns them all, and the per-lot figure is a filter away.
  const installments = useMyInstallments();

  return (
    <SafeAreaView style={styles.container}>
      <FlatList
        data={units.data ?? []}
        keyExtractor={(unit) => unit.unitId}
        contentContainerStyle={styles.content}
        ListHeaderComponent={
          <View style={styles.header}>
            {/* Titled after what it shows, like oikos-web's owner dashboard. */}
            <Text style={styles.greeting}>Mes lots</Text>
            <Card style={styles.linksCard}>
              <Button variant="secondary" onPress={() => navigation.navigate('MyInstallments')}>
                Mes échéances
              </Button>
              <Button variant="secondary" onPress={() => navigation.navigate('MyPayments')}>
                Mes paiements
              </Button>
              <Button variant="secondary" onPress={() => navigation.navigate('MyGeneralMeetings')}>
                Mes assemblées
              </Button>
            {/* No "Mes invitations" entry: the screen stays routed (and deep-linkable
                as oikos://my-membership-requests) but is deliberately not surfaced here. */}
            </Card>
          </View>
        }
        renderItem={({ item }: { item: OwnedUnit }) => (
          <MyUnitCard
            unit={item}
            onPress={() => navigation.navigate('MyUnitDetail', { unit: item })}
            outstanding={installments.data ? unitOutstanding(installments.data, item.unitId) : undefined}
          />
        )}
        ItemSeparatorComponent={() => <Text style={styles.separator} />}
        ListEmptyComponent={
          <>
            {units.isLoading && <Loader label="Chargement de vos lots…" />}
            {units.isError && <Alert message={getErrorMessage(units.error)} />}
            {units.isSuccess && (
              <EmptyState title="Aucun lot">Vous n'êtes propriétaire d'aucun lot pour le moment.</EmptyState>
            )}
          </>
        }
      />
    </SafeAreaView>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: colors.gray[50],
  },
  content: {
    padding: 16,
    flexGrow: 1,
  },
  header: {
    gap: 12,
    marginBottom: 16,
  },
  greeting: {
    fontSize: 18,
    fontWeight: '600',
    color: colors.gray[900],
  },
  linksCard: {
    marginBottom: 16,
    gap: 8,
  },
  separator: {
    height: 12,
  },
});
