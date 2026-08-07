import { Select } from '@/shared/components/Select/Select';
import { Input } from '@/shared/components/Input/Input';
import { INSTALLMENT_STATUS_LABELS } from '@/features/property-mngt/installments/constants/installmentStatusLabels';
import { useInstallmentCallsByProperty } from '@/features/property-mngt/installments/hooks/useInstallmentCallsByProperty';
import { formatPeriod } from '@/features/property-mngt/installments/utils/formatPeriod';
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
  installmentCallId: string;
  sortBy: InstallmentSortField;
  sortDirection: SortDirection;
}

// Every installment call of the property, fetched once for the "Appel de fonds"
// dropdown - a property rarely has more than a few dozen calls, so one large
// page (no pagination UI needed here) is simpler than a searchable picker.
const INSTALLMENT_CALLS_PAGE_SIZE = 100;

const SORT_FIELD_LABELS: Record<InstallmentSortField, string> = {
  DUE_DATE: "Date d'échéance",
  AMOUNT: 'Montant',
};

const SORT_DIRECTION_LABELS: Record<SortDirection, string> = {
  ASC: 'Croissant',
  DESC: 'Décroissant',
};

interface InstallmentFiltersProps {
  propertyId: string;
  value: InstallmentFiltersValue;
  onChange: (value: InstallmentFiltersValue) => void;
}

export function InstallmentFilters({ propertyId, value, onChange }: InstallmentFiltersProps) {
  const installmentCalls = useInstallmentCallsByProperty(propertyId, 0, INSTALLMENT_CALLS_PAGE_SIZE);

  return (
    <div className="grid grid-cols-1 gap-3 sm:grid-cols-2 lg:grid-cols-6">
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
      <Select
        label="Appel de fonds"
        name="installmentCallId"
        value={value.installmentCallId}
        onChange={(e) => onChange({ ...value, installmentCallId: e.target.value })}
      >
        <option value="">Tous</option>
        {installmentCalls.data?.content.map((call) => (
          <option key={call.id} value={call.id}>
            {formatPeriod(call.period)}
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
