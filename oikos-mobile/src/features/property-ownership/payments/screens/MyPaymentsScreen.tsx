import { useMemo, useState } from 'react';
import { FlatList, Pressable, StyleSheet, Text, View } from 'react-native';
import { SafeAreaView } from 'react-native-safe-area-context';
import type { NativeStackScreenProps } from '@react-navigation/native-stack';
import { useMyPayments } from '@/features/property-ownership/payments/hooks/useMyPayments';
import { useMyUnits } from '@/features/property-ownership/units/hooks/useMyUnits';
import { formatUnitLabel } from '@/features/property-ownership/units/utils/formatUnitLabel';
import { MyPaymentFilters } from '@/features/property-ownership/payments/components/MyPaymentFilters';
import {
  DEFAULT_MY_PAYMENT_FILTERS,
  filterPayments,
  paymentsTotal,
  type MyPaymentFiltersValue,
} from '@/features/property-ownership/payments/utils/filterPayments';
import { PAYMENT_MODE_LABELS } from '@/features/property-ownership/payments/constants/paymentModeLabels';
import { Loader } from '@/shared/components/Loader/Loader';
import { Alert } from '@/shared/components/Alert/Alert';
import { Badge } from '@/shared/components/Badge/Badge';
import { Card } from '@/shared/components/Card/Card';
import { EmptyState } from '@/shared/components/EmptyState/EmptyState';
import { FilterPanel } from '@/shared/components/FilterPanel/FilterPanel';
import { getErrorMessage } from '@/shared/utils/getErrorMessage';
import { countActiveFilters } from '@/shared/utils/countActiveFilters';
import { colors } from '@/shared/theme/colors';
import type { HomeStackParamList } from '@/app/navigation/HomeStackNavigator';
import type { Payment } from '@/features/property-ownership/payments/types/payment.types';

type Props = NativeStackScreenProps<HomeStackParamList, 'MyPayments'>;

// Sorting always holds a value, so it would inflate the "active filters" count
// on an untouched list - it is still reset by "Effacer".
const SORT_KEYS = ['sortBy', 'sortDirection'] as const;

export function MyPaymentsScreen({ navigation }: Props) {
  const payments = useMyPayments();
  const units = useMyUnits();
  const [filters, setFilters] = useState<MyPaymentFiltersValue>(DEFAULT_MY_PAYMENT_FILTERS);

  const isLoading = payments.isLoading || units.isLoading;
  const error = payments.error ?? units.error;

  const unitsById = useMemo(
    () => new Map((units.data ?? []).map((unit) => [unit.unitId, unit])),
    [units.data],
  );

  const unitLabelById = useMemo(
    () => new Map((units.data ?? []).map((unit) => [unit.unitId, formatUnitLabel(unit)])),
    [units.data],
  );

  const filtered = useMemo(
    () => filterPayments(payments.data ?? [], filters, unitLabelById),
    [payments.data, filters, unitLabelById],
  );

  return (
    <SafeAreaView style={styles.container}>
      <FlatList
        data={filtered}
        keyExtractor={(payment) => payment.id}
        contentContainerStyle={styles.content}
        ListHeaderComponent={
          <View style={styles.header}>
            {/* Total of the filtered rows, unlike the installments badge: there is
                nothing to "settle" here, so the useful figure is what the current
                selection adds up to. */}
            <View style={styles.totalBadge}>
              <Text style={styles.totalLabel}>Total versé</Text>
              <Text style={styles.totalAmount}>{paymentsTotal(filtered).toLocaleString('fr-FR')} MAD</Text>
            </View>
            <Card>
              <FilterPanel
                activeCount={countActiveFilters(filters, DEFAULT_MY_PAYMENT_FILTERS, SORT_KEYS)}
                onClear={() => setFilters(DEFAULT_MY_PAYMENT_FILTERS)}
                search={{
                  value: filters.search,
                  onChange: (search) => setFilters({ ...filters, search }),
                  placeholder: 'Rechercher un lot, un montant…',
                }}
              >
                <MyPaymentFilters value={filters} onChange={setFilters} />
              </FilterPanel>
            </Card>
          </View>
        }
        renderItem={({ item }: { item: Payment }) => {
          const unit = unitsById.get(item.unitId);
          return (
            <Pressable
              onPress={() => navigation.navigate('MyPaymentDetail', { paymentId: item.id })}
              style={styles.row}
            >
              <View style={styles.rowText}>
                {unit && <Text style={styles.unitLabel}>{formatUnitLabel(unit)}</Text>}
                <Text style={styles.rowLabel}>
                  Paiement du {new Date(item.valueDate).toLocaleDateString('fr-FR')} —{' '}
                  {item.amount.toLocaleString('fr-FR')} MAD
                </Text>
              </View>
              <Badge color="light">{PAYMENT_MODE_LABELS[item.mode]}</Badge>
            </Pressable>
          );
        }}
        ItemSeparatorComponent={() => <View style={styles.separator} />}
        ListEmptyComponent={
          <>
            {isLoading && <Loader label="Chargement de vos paiements…" />}
            {error && <Alert message={getErrorMessage(error)} />}
            {!isLoading && !error && (
              <EmptyState title="Aucun paiement">
                {(payments.data ?? []).length === 0
                  ? "Vous n'avez aucun paiement pour le moment."
                  : 'Aucun paiement ne correspond à ces filtres.'}
              </EmptyState>
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
  totalBadge: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'space-between',
    minHeight: 44,
    borderRadius: 12,
    backgroundColor: colors.brand[50],
    paddingHorizontal: 16,
    paddingVertical: 12,
  },
  totalLabel: {
    fontSize: 14,
    fontWeight: '500',
    color: colors.brand[500],
  },
  totalAmount: {
    fontSize: 16,
    fontWeight: '600',
    color: colors.brand[500],
  },
  row: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'space-between',
    gap: 12,
    paddingVertical: 8,
  },
  rowText: {
    flex: 1,
    gap: 2,
  },
  unitLabel: {
    fontSize: 13,
    color: colors.gray[500],
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
