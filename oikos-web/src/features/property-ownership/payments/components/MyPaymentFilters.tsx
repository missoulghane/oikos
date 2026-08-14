import { Select } from '@/shared/components/Select/Select';
import { Input } from '@/shared/components/Input/Input';
import { useMyUnits } from '@/features/property-ownership/units/hooks/useMyUnits';
import { PAYMENT_MODE_LABELS } from '@/features/property-mngt/installments/constants/paymentModeLabels';
import type { PaymentMode } from '@/features/property-mngt/installments/types/payment.types';
import type { MyPaymentFiltersValue } from '@/features/property-ownership/payments/utils/filterPayments';

const SORT_FIELD_LABELS: Record<MyPaymentFiltersValue['sortBy'], string> = {
  VALUE_DATE: 'Date de valeur',
  AMOUNT: 'Montant',
};

const SORT_DIRECTION_LABELS: Record<MyPaymentFiltersValue['sortDirection'], string> = {
  ASC: 'Croissant',
  DESC: 'Décroissant',
};

interface MyPaymentFiltersProps {
  value: MyPaymentFiltersValue;
  onChange: (value: MyPaymentFiltersValue) => void;
}

export function MyPaymentFilters({ value, onChange }: MyPaymentFiltersProps) {
  const units = useMyUnits();

  return (
    <div className="grid grid-cols-1 gap-3 sm:grid-cols-2 lg:grid-cols-5">
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
      <div className="grid grid-cols-2 gap-3">
        <Select
          label="Trier par"
          name="sortBy"
          value={value.sortBy}
          onChange={(e) => onChange({ ...value, sortBy: e.target.value as MyPaymentFiltersValue['sortBy'] })}
        >
          {(Object.keys(SORT_FIELD_LABELS) as MyPaymentFiltersValue['sortBy'][]).map((field) => (
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
            onChange({ ...value, sortDirection: e.target.value as MyPaymentFiltersValue['sortDirection'] })
          }
        >
          {(Object.keys(SORT_DIRECTION_LABELS) as MyPaymentFiltersValue['sortDirection'][]).map((direction) => (
            <option key={direction} value={direction}>
              {SORT_DIRECTION_LABELS[direction]}
            </option>
          ))}
        </Select>
      </div>
    </div>
  );
}
