import { Select } from '@/shared/components/Select/Select';
import { Input } from '@/shared/components/Input/Input';
import { useMyUnits } from '@/features/property-ownership/units/hooks/useMyUnits';
import type { MyInstallmentFiltersValue } from '@/features/property-ownership/installments/utils/filterInstallments';
import { NotYetDueToggle } from '@/features/property-mngt/installments/components/NotYetDueToggle';

// Owner-facing wording: the three raw statuses collapse into the two states an
// owner actually cares about ("est-ce que je dois encore quelque chose ?"), so
// a partially settled echeance shows up under "À régler" rather than as its own
// option (see isDue).
const STATUS_LABELS: Record<'DUE' | 'SETTLED', string> = {
  DUE: 'À régler',
  SETTLED: 'Payée',
};

// Sorting is not offered here: it is driven by clicking the table's column
// headers instead (see SortableColumnHeader).
interface MyInstallmentFiltersProps {
  value: MyInstallmentFiltersValue;
  onChange: (value: MyInstallmentFiltersValue) => void;
  /** Rows the "à venir" toggle is currently withholding. */
  hiddenNotYetDue: number;
}

export function MyInstallmentFilters({ value, onChange, hiddenNotYetDue }: MyInstallmentFiltersProps) {
  const units = useMyUnits();

  return (
    // Five columns from lg so the whole set stays on one row; below that it
    // wraps rather than squeezing date inputs to unusable widths.
    <div className="grid grid-cols-1 gap-3 sm:grid-cols-2 lg:grid-cols-5">
      <Select
        label="Statut"
        name="status"
        value={value.status}
        onChange={(e) => onChange({ ...value, status: e.target.value as MyInstallmentFiltersValue['status'] })}
      >
        <option value="">Tous</option>
        <option value="DUE">{STATUS_LABELS.DUE}</option>
        <option value="SETTLED">{STATUS_LABELS.SETTLED}</option>
      </Select>
      <Select
        label="Lot"
        name="unitId"
        value={value.unitId}
        onChange={(e) => onChange({ ...value, unitId: e.target.value })}
      >
        <option value="">Tous</option>
        {units.data?.map((unit) => (
          <option key={unit.unitId} value={unit.unitId}>
            {unit.propertyName} — {unit.buildingName} — {unit.unitNumber}
          </option>
        ))}
      </Select>
      <Input
        type="date"
        label="Échéance à partir du"
        name="dueDateFrom"
        value={value.dueDateFrom}
        onChange={(e) => onChange({ ...value, dueDateFrom: e.target.value })}
      />
      <Input
        type="date"
        label="Échéance jusqu'au"
        name="dueDateTo"
        value={value.dueDateTo}
        onChange={(e) => onChange({ ...value, dueDateTo: e.target.value })}
      />
      <NotYetDueToggle
        checked={value.includeNotYetDue}
        hiddenCount={hiddenNotYetDue}
        onChange={(includeNotYetDue) => onChange({ ...value, includeNotYetDue })}
      />
    </div>
  );
}
