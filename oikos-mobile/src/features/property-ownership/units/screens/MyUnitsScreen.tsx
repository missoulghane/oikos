import { FlatList, StyleSheet, Text } from 'react-native';
import { SafeAreaView } from 'react-native-safe-area-context';
import type { NativeStackScreenProps } from '@react-navigation/native-stack';
import { useMyUnits } from '@/features/property-ownership/units/hooks/useMyUnits';
import { MyUnitCard } from '@/features/property-ownership/units/components/MyUnitCard';
import { Loader } from '@/shared/components/Loader/Loader';
import { Alert } from '@/shared/components/Alert/Alert';
import { EmptyState } from '@/shared/components/EmptyState/EmptyState';
import { getErrorMessage } from '@/shared/utils/getErrorMessage';
import { colors } from '@/shared/theme/colors';
import type { MainStackParamList } from '@/app/navigation/MainNavigator';
import type { OwnedUnit } from '@/features/property-ownership/units/types/unit.types';

type Props = NativeStackScreenProps<MainStackParamList, 'MyUnits'>;

export function MyUnitsScreen({ navigation }: Props) {
  const units = useMyUnits();

  return (
    <SafeAreaView style={styles.container}>
      <FlatList
        data={units.data ?? []}
        keyExtractor={(unit) => unit.unitId}
        contentContainerStyle={styles.content}
        ListHeaderComponent={<Text style={styles.title}>Mes lots</Text>}
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
  title: {
    marginBottom: 16,
    fontSize: 18,
    fontWeight: '600',
    color: colors.gray[900],
  },
  separator: {
    height: 12,
  },
});
