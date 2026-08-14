import { FlatList, StyleSheet, Text } from 'react-native';
import { SafeAreaView } from 'react-native-safe-area-context';
import type { NativeStackScreenProps } from '@react-navigation/native-stack';
import { useMyUnits } from '@/features/property-ownership/units/hooks/useMyUnits';
import { MyUnitCard } from '@/features/property-ownership/units/components/MyUnitCard';
import { Button } from '@/shared/components/Button/Button';
import { Card } from '@/shared/components/Card/Card';
import { Loader } from '@/shared/components/Loader/Loader';
import { Alert } from '@/shared/components/Alert/Alert';
import { EmptyState } from '@/shared/components/EmptyState/EmptyState';
import { getErrorMessage } from '@/shared/utils/getErrorMessage';
import { colors } from '@/shared/theme/colors';
import type { UnitsStackParamList } from '@/app/navigation/UnitsStackNavigator';
import type { OwnedUnit } from '@/features/property-ownership/units/types/unit.types';

type Props = NativeStackScreenProps<UnitsStackParamList, 'MyUnits'>;

export function MyUnitsScreen({ navigation }: Props) {
  const units = useMyUnits();

  return (
    <SafeAreaView style={styles.container}>
      <FlatList
        data={units.data ?? []}
        keyExtractor={(unit) => unit.unitId}
        contentContainerStyle={styles.content}
        ListHeaderComponent={
          <Card style={styles.linksCard}>
            <Button variant="secondary" onPress={() => navigation.navigate('MyInstallments')}>
              Mes échéances
            </Button>
            <Button variant="secondary" onPress={() => navigation.navigate('MyPayments')}>
              Mes paiements
            </Button>
            <Button variant="secondary" onPress={() => navigation.navigate('MyMembershipRequests')}>
              Mes invitations
            </Button>
          </Card>
        }
        renderItem={({ item }: { item: OwnedUnit }) => (
          <MyUnitCard unit={item} onPress={() => navigation.navigate('MyUnitDetail', { unit: item })} />
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
  linksCard: {
    marginBottom: 16,
    gap: 8,
  },
  separator: {
    height: 12,
  },
});
