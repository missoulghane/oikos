import { Select } from '@/shared/components/Select/Select';
import { NotYetDueToggle } from '@/features/property-mngt/installments/components/NotYetDueToggle';
import { Input } from '@/shared/components/Input/Input';
import { INSTALLMENT_STATUS_LABELS } from '@/features/property-mngt/installments/constants/installmentStatusLabels';
import { useInstallmentCallsByProperty } from '@/features/property-mngt/installments/hooks/useInstallmentCallsByProperty';
import { formatPeriod } from '@/features/property-mngt/installments/utils/formatPeriod';
import type {
  InstallmentSortField,
  InstallmentStatus,
  SortDirection,
} from '@/features/property-mngt/installments/types/installment.types';

export interface InstallmentFiltersValue {
  status: InstallmentStatus | '';
  search: string;
  /** Same default as the owner space: what is not owed yet stays out of sight. */
  includeNotYetDue: boolean;
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

interface InstallmentFiltersProps {
  propertyId: string;
  value: InstallmentFiltersValue;
  onChange: (value: InstallmentFiltersValue) => void;
}

export function InstallmentFilters({ propertyId, value, onChange }: InstallmentFiltersProps) {
  const installmentCalls = useInstallmentCallsByProperty(propertyId, 0, INSTALLMENT_CALLS_PAGE_SIZE);

  return (
    // Five columns from lg: with sorting moved onto the table headers, the
    // whole set fits one row without squeezing the date inputs.
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
      <NotYetDueToggle
        checked={value.includeNotYetDue}
        onChange={(includeNotYetDue) => onChange({ ...value, includeNotYetDue })}
      />
    </div>
  );
}
