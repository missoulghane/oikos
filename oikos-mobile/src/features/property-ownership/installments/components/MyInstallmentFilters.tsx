import { StyleSheet, View } from 'react-native';
import { Select, type SelectOption } from '@/shared/components/Select/Select';
import { useMyUnits } from '@/features/property-ownership/units/hooks/useMyUnits';
import { formatUnitLabel } from '@/features/property-ownership/units/utils/formatUnitLabel';
import { DATE_RANGE_PRESET_LABELS, type DateRangePreset } from '@/shared/utils/datePresets';
import type { MyInstallmentFiltersValue } from '@/features/property-ownership/installments/utils/filterInstallments';
import { NotYetDueToggle } from '@/features/property-ownership/installments/components/NotYetDueToggle';

const STATUS_OPTIONS: SelectOption[] = [
  { value: '', label: 'Tous' },
  { value: 'DUE', label: 'À régler' },
  { value: 'SETTLED', label: 'Payée' },
];

const SORT_OPTIONS: SelectOption[] = [
  { value: 'DUE_DATE:DESC', label: "Date d'échéance (récent)" },
  { value: 'DUE_DATE:ASC', label: "Date d'échéance (ancien)" },
  { value: 'AMOUNT:DESC', label: 'Montant (décroissant)' },
  { value: 'AMOUNT:ASC', label: 'Montant (croissant)' },
];

const PERIOD_OPTIONS: SelectOption[] = (
  Object.keys(DATE_RANGE_PRESET_LABELS) as DateRangePreset[]
).map((preset) => ({ value: preset, label: DATE_RANGE_PRESET_LABELS[preset] }));

interface MyInstallmentFiltersProps {
  value: MyInstallmentFiltersValue;
  onChange: (value: MyInstallmentFiltersValue) => void;
  /** Rows the "à échoir" toggle is currently withholding. */
  hiddenNotYetDue: number;
}

export function MyInstallmentFilters({ value, onChange, hiddenNotYetDue }: MyInstallmentFiltersProps) {
  const units = useMyUnits();

  const unitOptions: SelectOption[] = [
    { value: '', label: 'Tous' },
    ...(units.data ?? []).map((unit) => ({ value: unit.unitId, label: formatUnitLabel(unit) })),
  ];

  // Sort field and direction are one control on mobile: two side-by-side
  // pickers for what reads as a single choice costs a whole extra row of screen.
  return (
    <View style={styles.container}>
      <Select
        label="Statut"
        value={value.status}
        options={STATUS_OPTIONS}
        onChange={(next) => onChange({ ...value, status: next as MyInstallmentFiltersValue['status'] })}
      />
      <Select
        label="Lot"
        value={value.unitId}
        options={unitOptions}
        onChange={(next) => onChange({ ...value, unitId: next })}
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
            sortBy: sortBy as MyInstallmentFiltersValue['sortBy'],
            sortDirection: sortDirection as MyInstallmentFiltersValue['sortDirection'],
          });
        }}
      />
      <NotYetDueToggle
        checked={value.includeNotYetDue}
        hiddenCount={hiddenNotYetDue}
        onChange={(includeNotYetDue) => onChange({ ...value, includeNotYetDue })}
      />
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    gap: 12,
  },
});
