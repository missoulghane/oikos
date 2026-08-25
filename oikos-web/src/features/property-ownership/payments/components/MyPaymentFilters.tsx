import { Select } from '@/shared/components/Select/Select';
import { Input } from '@/shared/components/Input/Input';
import { useMyUnits } from '@/features/property-ownership/units/hooks/useMyUnits';
import { PAYMENT_MODE_LABELS } from '@/features/property-mngt/installments/constants/paymentModeLabels';
import type { PaymentMode } from '@/features/property-mngt/installments/types/payment.types';
import type { MyPaymentFiltersValue } from '@/features/property-ownership/payments/utils/filterPayments';

// Sorting is not offered here: it is driven by clicking the table's column
// headers instead (see SortableColumnHeader).
interface MyPaymentFiltersProps {
  value: MyPaymentFiltersValue;
  onChange: (value: MyPaymentFiltersValue) => void;
}

export function MyPaymentFilters({ value, onChange }: MyPaymentFiltersProps) {
  const units = useMyUnits();

  return (
    <div className="grid grid-cols-1 gap-3 sm:grid-cols-2 lg:grid-cols-4">
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
      <Select
        label="Mode"
        name="mode"
        value={value.mode}
        onChange={(e) => onChange({ ...value, mode: e.target.value as PaymentMode | '' })}
      >
        <option value="">Tous</option>
        {(Object.keys(PAYMENT_MODE_LABELS) as PaymentMode[]).map((mode) => (
          <option key={mode} value={mode}>
            {PAYMENT_MODE_LABELS[mode]}
          </option>
        ))}
      </Select>
      <Input
        type="date"
        label="Payé à partir du"
        name="valueDateFrom"
        value={value.valueDateFrom}
        onChange={(e) => onChange({ ...value, valueDateFrom: e.target.value })}
      />
      <Input
        type="date"
        label="Payé jusqu'au"
        name="valueDateTo"
        value={value.valueDateTo}
        onChange={(e) => onChange({ ...value, valueDateTo: e.target.value })}
      />
    </div>
  );
}
