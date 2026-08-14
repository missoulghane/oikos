import { FlatList, Pressable, StyleSheet, Text, View } from 'react-native';
import { SafeAreaView } from 'react-native-safe-area-context';
import type { NativeStackScreenProps } from '@react-navigation/native-stack';
import { useMyPayments } from '@/features/property-ownership/payments/hooks/useMyPayments';
import { useMyUnits } from '@/features/property-ownership/units/hooks/useMyUnits';
import { PAYMENT_MODE_LABELS } from '@/features/property-ownership/payments/constants/paymentModeLabels';
import { Loader } from '@/shared/components/Loader/Loader';
import { Alert } from '@/shared/components/Alert/Alert';
import { Badge } from '@/shared/components/Badge/Badge';
import { EmptyState } from '@/shared/components/EmptyState/EmptyState';
import { getErrorMessage } from '@/shared/utils/getErrorMessage';
import { colors } from '@/shared/theme/colors';
import type { UnitsStackParamList } from '@/app/navigation/UnitsStackNavigator';
import type { Payment } from '@/features/property-ownership/payments/types/payment.types';

type Props = NativeStackScreenProps<UnitsStackParamList, 'MyPayments'>;

export function MyPaymentsScreen({ navigation }: Props) {
  const payments = useMyPayments();
  const units = useMyUnits();

  const isLoading = payments.isLoading || units.isLoading;
  const error = payments.error ?? units.error;
  const unitsById = new Map((units.data ?? []).map((unit) => [unit.unitId, unit]));

  const sorted = (payments.data ?? [])
    .slice()
    .sort((a, b) => new Date(b.valueDate).getTime() - new Date(a.valueDate).getTime());

  return (
    <SafeAreaView style={styles.container}>
      <FlatList
        data={sorted}
        keyExtractor={(payment) => payment.id}
        contentContainerStyle={styles.content}
        renderItem={({ item }: { item: Payment }) => {
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
                  Paiement du {new Date(item.valueDate).toLocaleDateString('fr-FR')} — {item.amount.toLocaleString('fr-FR')}{' '}
                  MAD
                </Text>
              </View>
              <Badge color="light">{PAYMENT_MODE_LABELS[item.mode]}</Badge>
            </View>
          );
        }}
        ItemSeparatorComponent={() => <View style={styles.separator} />}
        ListEmptyComponent={
          <>
            {isLoading && <Loader label="Chargement de vos paiements…" />}
            {error && <Alert message={getErrorMessage(error)} />}
            {!isLoading && !error && (
              <EmptyState title="Aucun paiement">Vous n'avez aucun paiement pour le moment.</EmptyState>
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
