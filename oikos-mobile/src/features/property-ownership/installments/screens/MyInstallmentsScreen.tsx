import { useMemo, useState } from 'react';
import { FlatList, Pressable, StyleSheet, Text, View } from 'react-native';
import { SafeAreaView } from 'react-native-safe-area-context';
import type { NativeStackScreenProps } from '@react-navigation/native-stack';
import { useMyInstallments } from '@/features/property-ownership/installments/hooks/useMyInstallments';
import { useMyUnits } from '@/features/property-ownership/units/hooks/useMyUnits';
import { formatUnitLabel } from '@/features/property-ownership/units/utils/formatUnitLabel';
import { MyInstallmentFilters } from '@/features/property-ownership/installments/components/MyInstallmentFilters';
import { outstandingTotal } from '@/features/property-ownership/installments/utils/installmentTotals';
import {
  DEFAULT_MY_INSTALLMENT_FILTERS,
  filterInstallments,
  type MyInstallmentFiltersValue,
} from '@/features/property-ownership/installments/utils/filterInstallments';
import {
  INSTALLMENT_STATUS_BADGE_COLORS,
  INSTALLMENT_STATUS_LABELS,
} from '@/features/property-ownership/installments/constants/installmentStatusLabels';
import { Loader } from '@/shared/components/Loader/Loader';
import { Alert } from '@/shared/components/Alert/Alert';
import { Badge } from '@/shared/components/Badge/Badge';
import { Card } from '@/shared/components/Card/Card';
import { EmptyState } from '@/shared/components/EmptyState/EmptyState';
import { FilterPanel } from '@/shared/components/FilterPanel/FilterPanel';
import { getErrorMessage } from '@/shared/utils/getErrorMessage';
import { countActiveFilters } from '@/shared/utils/countActiveFilters';
import { colors } from '@/shared/theme/colors';
import type { UnitsStackParamList } from '@/app/navigation/UnitsStackNavigator';
import type { OwnedInstallment } from '@/features/property-ownership/installments/types/installment.types';

type Props = NativeStackScreenProps<UnitsStackParamList, 'MyInstallments'>;

// Sorting always holds a value, so it would inflate the "active filters" count
// on an untouched list - it is still reset by "Effacer".
const SORT_KEYS = ['sortBy', 'sortDirection'] as const;

export function MyInstallmentsScreen({ navigation }: Props) {
  const installments = useMyInstallments();
  const units = useMyUnits();
  const [filters, setFilters] = useState<MyInstallmentFiltersValue>(DEFAULT_MY_INSTALLMENT_FILTERS);

  const isLoading = installments.isLoading || units.isLoading;
  const error = installments.error ?? units.error;

  const unitsById = useMemo(
    () => new Map((units.data ?? []).map((unit) => [unit.unitId, unit])),
    [units.data],
  );

  // Over the whole dataset, not the filtered rows: the badge states what is
  // owed in total, and tapping it is what narrows the list.
  const totalDue = outstandingTotal(installments.data ?? []);

  const unitLabelById = useMemo(
    () => new Map((units.data ?? []).map((unit) => [unit.unitId, formatUnitLabel(unit)])),
    [units.data],
  );

  const filtered = useMemo(
    () => filterInstallments(installments.data ?? [], filters, unitLabelById),
    [installments.data, filters, unitLabelById],
  );

  const isDueFilterActive = filters.status === 'DUE';

  return (
    <SafeAreaView style={styles.container}>
      <FlatList
        data={filtered}
        keyExtractor={(installment) => installment.id}
        contentContainerStyle={styles.content}
        ListHeaderComponent={
          <View style={styles.header}>
            <Pressable
              accessibilityRole="button"
              accessibilityState={{ selected: isDueFilterActive }}
              onPress={() => setFilters({ ...filters, status: isDueFilterActive ? '' : 'DUE' })}
              style={[styles.totalBadge, isDueFilterActive && styles.totalBadgeActive]}
            >
              <Text style={[styles.totalLabel, isDueFilterActive && styles.totalTextActive]}>Total à régler</Text>
              <Text style={[styles.totalAmount, isDueFilterActive && styles.totalTextActive]}>
                {totalDue.toLocaleString('fr-FR')} MAD
              </Text>
            </Pressable>
            <Card>
              <FilterPanel
                activeCount={countActiveFilters(filters, DEFAULT_MY_INSTALLMENT_FILTERS, SORT_KEYS)}
                onClear={() => setFilters(DEFAULT_MY_INSTALLMENT_FILTERS)}
                search={{
                  value: filters.search,
                  onChange: (search) => setFilters({ ...filters, search }),
                  placeholder: 'Rechercher un lot, un montant…',
                }}
              >
                <MyInstallmentFilters value={filters} onChange={setFilters} />
              </FilterPanel>
            </Card>
          </View>
        }
        renderItem={({ item }: { item: OwnedInstallment }) => {
          const unit = unitsById.get(item.unitId);
          return (
            <Pressable
              onPress={() => navigation.navigate('MyInstallmentDetail', { installmentId: item.id })}
              style={styles.row}
            >
              <View style={styles.rowText}>
                {unit && <Text style={styles.unitLabel}>{formatUnitLabel(unit)}</Text>}
                <Text style={styles.rowLabel}>
                  Échéance du {new Date(item.dueDate).toLocaleDateString('fr-FR')} —{' '}
                  {item.amount.toLocaleString('fr-FR')} MAD
                </Text>
                <Text style={styles.rowOutstanding}>
                  Reste à payer : {item.outstandingAmount.toLocaleString('fr-FR')} MAD
                </Text>
              </View>
              <Badge color={INSTALLMENT_STATUS_BADGE_COLORS[item.status]}>
                {INSTALLMENT_STATUS_LABELS[item.status]}
              </Badge>
            </Pressable>
          );
        }}
        ItemSeparatorComponent={() => <View style={styles.separator} />}
        ListEmptyComponent={
          <>
            {isLoading && <Loader label="Chargement de vos échéances…" />}
            {error && <Alert message={getErrorMessage(error)} />}
            {!isLoading && !error && (
              <EmptyState title="Aucune échéance">
                {(installments.data ?? []).length === 0
                  ? "Vous n'avez aucune échéance pour le moment."
                  : 'Aucune échéance ne correspond à ces filtres.'}
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
  totalBadgeActive: {
    backgroundColor: colors.brand[500],
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
  totalTextActive: {
    color: colors.white,
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
  rowOutstanding: {
    fontSize: 13,
    color: colors.gray[500],
  },
  separator: {
    height: 1,
    backgroundColor: colors.gray[100],
    marginVertical: 8,
  },
});
