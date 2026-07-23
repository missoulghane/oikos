import { Select } from '@/shared/components/Select/Select';
import { Input } from '@/shared/components/Input/Input';
import { INSTALLMENT_STATUS_LABELS } from '@/features/property-mngt/installments/constants/installmentStatusLabels';
import {
  INSTALLMENT_SORT_FIELDS,
  type InstallmentSortField,
  type InstallmentStatus,
  type SortDirection,
} from '@/features/property-mngt/installments/types/installment.types';

export interface InstallmentFiltersValue {
  status: InstallmentStatus | '';
  dueDateFrom: string;
  dueDateTo: string;
  sortBy: InstallmentSortField;
  sortDirection: SortDirection;
}

const SORT_FIELD_LABELS: Record<InstallmentSortField, string> = {
  DUE_DATE: "Date d'échéance",
  AMOUNT: 'Montant',
};

const SORT_DIRECTION_LABELS: Record<SortDirection, string> = {
  ASC: 'Croissant',
  DESC: 'Décroissant',
};

interface InstallmentFiltersProps {
  value: InstallmentFiltersValue;
  onChange: (value: InstallmentFiltersValue) => void;
}

export function InstallmentFilters({ value, onChange }: InstallmentFiltersProps) {
  return (
    <div className="grid grid-cols-1 gap-3 sm:grid-cols-2 lg:grid-cols-5">
      <Select
        label="Statut"
        name="status"
        value={value.status}
        onChange={(e) => onChange({ ...value, status: e.target.value as InstallmentStatus | '' })}
      >
        <option value="">Tous</option>
        {(Object.keys(INSTALLMENT_STATUS_LABELS) as InstallmentStatus[]).map((status) => (
          <option key={status} value={status}>
            {INSTALLMENT_STATUS_LABELS[status]}
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
      <Select
        label="Trier par"
        name="sortBy"
        value={value.sortBy}
        onChange={(e) => onChange({ ...value, sortBy: e.target.value as InstallmentSortField })}
      >
        {INSTALLMENT_SORT_FIELDS.map((field) => (
          <option key={field} value={field}>
            {SORT_FIELD_LABELS[field]}
          </option>
        ))}
      </Select>
      <Select
        label="Ordre"
        name="sortDirection"
        value={value.sortDirection}
        onChange={(e) => onChange({ ...value, sortDirection: e.target.value as SortDirection })}
      >
        {(Object.keys(SORT_DIRECTION_LABELS) as SortDirection[]).map((direction) => (
          <option key={direction} value={direction}>
            {SORT_DIRECTION_LABELS[direction]}
          </option>
        ))}
      </Select>
    </div>
  );
}
