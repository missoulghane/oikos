import { StyleSheet, View } from 'react-native';
import { Select, type SelectOption } from '@/shared/components/Select/Select';
import { useMyUnits } from '@/features/property-ownership/units/hooks/useMyUnits';
import { formatUnitLabel } from '@/features/property-ownership/units/utils/formatUnitLabel';
import { PAYMENT_MODE_LABELS } from '@/features/property-ownership/payments/constants/paymentModeLabels';
import type { PaymentMode } from '@/features/property-ownership/payments/types/payment.types';
import { DATE_RANGE_PRESET_LABELS, type DateRangePreset } from '@/shared/utils/datePresets';
import type { MyPaymentFiltersValue } from '@/features/property-ownership/payments/utils/filterPayments';

const MODE_OPTIONS: SelectOption[] = [
  { value: '', label: 'Tous' },
  ...(Object.keys(PAYMENT_MODE_LABELS) as PaymentMode[]).map((mode) => ({
    value: mode,
    label: PAYMENT_MODE_LABELS[mode],
  })),
];

const SORT_OPTIONS: SelectOption[] = [
  { value: 'VALUE_DATE:DESC', label: 'Date de valeur (récent)' },
  { value: 'VALUE_DATE:ASC', label: 'Date de valeur (ancien)' },
  { value: 'AMOUNT:DESC', label: 'Montant (décroissant)' },
  { value: 'AMOUNT:ASC', label: 'Montant (croissant)' },
];

const PERIOD_OPTIONS: SelectOption[] = (
  Object.keys(DATE_RANGE_PRESET_LABELS) as DateRangePreset[]
).map((preset) => ({ value: preset, label: DATE_RANGE_PRESET_LABELS[preset] }));

interface MyPaymentFiltersProps {
  value: MyPaymentFiltersValue;
  onChange: (value: MyPaymentFiltersValue) => void;
}

export function MyPaymentFilters({ value, onChange }: MyPaymentFiltersProps) {
  const units = useMyUnits();

  const unitOptions: SelectOption[] = [
    { value: '', label: 'Tous' },
    ...(units.data ?? []).map((unit) => ({ value: unit.unitId, label: formatUnitLabel(unit) })),
  ];

  return (
    <View style={styles.container}>
      <Select
        label="Lot"
        value={value.unitId}
        options={unitOptions}
        onChange={(next) => onChange({ ...value, unitId: next })}
      />
      <Select
        label="Mode"
        value={value.mode}
        options={MODE_OPTIONS}
        onChange={(next) => onChange({ ...value, mode: next as PaymentMode | '' })}
      />
      <Select
        label="Période"
        value={value.period}
        options={PERIOD_OPTIONS}
        onChange={(next) => onChange({ ...value, period: next as DateRangePreset })}
      />
      <Select
        label="Trier par"
        value={`${value.sortBy}:${value.sortDirection}`}
        options={SORT_OPTIONS}
        onChange={(next) => {
          const [sortBy, sortDirection] = next.split(':');
          onChange({
            ...value,
            sortBy: sortBy as MyPaymentFiltersValue['sortBy'],
            sortDirection: sortDirection as MyPaymentFiltersValue['sortDirection'],
          });
        }}
      />
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    gap: 12,
  },
});
