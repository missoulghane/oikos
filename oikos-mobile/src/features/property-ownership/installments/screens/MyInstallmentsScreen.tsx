import { FlatList, Pressable, StyleSheet, Text, View } from 'react-native';
import { SafeAreaView } from 'react-native-safe-area-context';
import type { NativeStackScreenProps } from '@react-navigation/native-stack';
import { useMyInstallments } from '@/features/property-ownership/installments/hooks/useMyInstallments';
import { useMyUnits } from '@/features/property-ownership/units/hooks/useMyUnits';
import {
  INSTALLMENT_STATUS_BADGE_COLORS,
  INSTALLMENT_STATUS_LABELS,
} from '@/features/property-ownership/installments/constants/installmentStatusLabels';
import { Loader } from '@/shared/components/Loader/Loader';
import { Alert } from '@/shared/components/Alert/Alert';
import { Badge } from '@/shared/components/Badge/Badge';
import { EmptyState } from '@/shared/components/EmptyState/EmptyState';
import { getErrorMessage } from '@/shared/utils/getErrorMessage';
import { colors } from '@/shared/theme/colors';
import type { UnitsStackParamList } from '@/app/navigation/UnitsStackNavigator';
import type { Installment } from '@/features/property-ownership/installments/types/installment.types';

type Props = NativeStackScreenProps<UnitsStackParamList, 'MyInstallments'>;

export function MyInstallmentsScreen({ navigation }: Props) {
  const installments = useMyInstallments();
  const units = useMyUnits();

  const isLoading = installments.isLoading || units.isLoading;
  const error = installments.error ?? units.error;
  const unitsById = new Map((units.data ?? []).map((unit) => [unit.unitId, unit]));

  const sorted = (installments.data ?? [])
    .slice()
    .sort((a, b) => new Date(b.dueDate).getTime() - new Date(a.dueDate).getTime());

  return (
    <SafeAreaView style={styles.container}>
      <FlatList
        data={sorted}
        keyExtractor={(installment) => installment.id}
        contentContainerStyle={styles.content}
        renderItem={({ item }: { item: Installment }) => {
          const unit = unitsById.get(item.unitId);
          return (
            <View style={styles.row}>
              <View style={styles.rowText}>
                {unit && (
                  <Pressable onPress={() => navigation.navigate('MyUnitDetail', { unit })}>
                    <Text style={styles.unitLink}>
                      {unit.propertyName} — {unit.buildingName} — Lot {unit.unitNumber}
                    </Text>
                  </Pressable>
                )}
                <Text style={styles.rowLabel}>
                  Échéance du {new Date(item.dueDate).toLocaleDateString('fr-FR')} —{' '}
                  {item.amount.toLocaleString('fr-FR')} MAD
                  {item.status === 'PARTIALLY_SETTLED' && ` (reste ${item.outstandingAmount.toLocaleString('fr-FR')} MAD)`}
                </Text>
              </View>
              <Badge color={INSTALLMENT_STATUS_BADGE_COLORS[item.status]}>{INSTALLMENT_STATUS_LABELS[item.status]}</Badge>
            </View>
          );
        }}
        ItemSeparatorComponent={() => <View style={styles.separator} />}
        ListEmptyComponent={
          <>
            {isLoading && <Loader label="Chargement de vos échéances…" />}
            {error && <Alert message={getErrorMessage(error)} />}
            {!isLoading && !error && (
              <EmptyState title="Aucune échéance">Vous n'avez aucune échéance pour le moment.</EmptyState>
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
  row: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'space-between',
    gap: 12,
    paddingVertical: 4,
  },
  rowText: {
    flex: 1,
    gap: 2,
  },
  unitLink: {
    fontSize: 13,
    color: colors.gray[500],
    textDecorationLine: 'underline',
  },
  rowLabel: {
    fontSize: 14,
    color: colors.gray[700],
  },
  separator: {
    height: 1,
    backgroundColor: colors.gray[100],
    marginVertical: 8,
  },
});
