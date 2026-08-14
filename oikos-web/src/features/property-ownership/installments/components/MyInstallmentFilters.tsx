import { Select } from '@/shared/components/Select/Select';
import { Input } from '@/shared/components/Input/Input';
import { useMyUnits } from '@/features/property-ownership/units/hooks/useMyUnits';
import type { MyInstallmentFiltersValue } from '@/features/property-ownership/installments/utils/filterInstallments';

// Owner-facing wording: the three raw statuses collapse into the two states an
// owner actually cares about ("est-ce que je dois encore quelque chose ?"), so
// a partially settled echeance shows up under "À régler" rather than as its own
// option (see isDue).
const STATUS_LABELS: Record<'DUE' | 'SETTLED', string> = {
  DUE: 'À régler',
  SETTLED: 'Payée',
};

const SORT_FIELD_LABELS: Record<MyInstallmentFiltersValue['sortBy'], string> = {
  DUE_DATE: "Date d'échéance",
  AMOUNT: 'Montant',
};

const SORT_DIRECTION_LABELS: Record<MyInstallmentFiltersValue['sortDirection'], string> = {
  ASC: 'Croissant',
  DESC: 'Décroissant',
};

interface MyInstallmentFiltersProps {
  value: MyInstallmentFiltersValue;
  onChange: (value: MyInstallmentFiltersValue) => void;
}

export function MyInstallmentFilters({ value, onChange }: MyInstallmentFiltersProps) {
  const units = useMyUnits();

  return (
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
            {unit.propertyName} — {unit.buildingName} — Lot {unit.unitNumber}
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
      <div className="grid grid-cols-2 gap-3">
        <Select
          label="Trier par"
          name="sortBy"
          value={value.sortBy}
          onChange={(e) => onChange({ ...value, sortBy: e.target.value as MyInstallmentFiltersValue['sortBy'] })}
        >
          {(Object.keys(SORT_FIELD_LABELS) as MyInstallmentFiltersValue['sortBy'][]).map((field) => (
            <option key={field} value={field}>
              {SORT_FIELD_LABELS[field]}
            </option>
          ))}
        </Select>
        <Select
          label="Ordre"
          name="sortDirection"
          value={value.sortDirection}
          onChange={(e) =>
            onChange({ ...value, sortDirection: e.target.value as MyInstallmentFiltersValue['sortDirection'] })
          }
        >
          {(Object.keys(SORT_DIRECTION_LABELS) as MyInstallmentFiltersValue['sortDirection'][]).map((direction) => (
            <option key={direction} value={direction}>
              {SORT_DIRECTION_LABELS[direction]}
            </option>
          ))}
        </Select>
      </div>
    </div>
  );
}
