import { Select } from '@/shared/components/Select/Select';
import { Input } from '@/shared/components/Input/Input';
import { FINANCIAL_ENTRY_TYPE_LABELS } from '@/features/property-mngt/accounting/constants/financialEntryLabels';
import type { FinancialAccount, FinancialEntryType } from '@/features/property-mngt/accounting/types/accounting.types';

export interface JournalFiltersValue {
  financialAccountId: string;
  type: FinancialEntryType | '';
  dateFrom: string;
  dateTo: string;
}

interface JournalFiltersProps {
  value: JournalFiltersValue;
  onChange: (value: JournalFiltersValue) => void;
  accounts: FinancialAccount[];
}

export function JournalFilters({ value, onChange, accounts }: JournalFiltersProps) {
  return (
    <div className="grid grid-cols-1 gap-3 sm:grid-cols-2 lg:grid-cols-4">
      <Select
        label="Compte"
        name="financialAccountId"
        value={value.financialAccountId}
        onChange={(e) => onChange({ ...value, financialAccountId: e.target.value })}
      >
        <option value="">Tous</option>
        {accounts.map((account) => (
          <option key={account.id} value={account.id}>
            {account.name}
          </option>
        ))}
      </Select>
      <Select
        label="Type"
        name="type"
        value={value.type}
        onChange={(e) => onChange({ ...value, type: e.target.value as FinancialEntryType | '' })}
      >
        <option value="">Tous</option>
        {(Object.keys(FINANCIAL_ENTRY_TYPE_LABELS) as FinancialEntryType[]).map((type) => (
          <option key={type} value={type}>
            {FINANCIAL_ENTRY_TYPE_LABELS[type]}
          </option>
        ))}
      </Select>
      <Input
        type="date"
        label="Du"
        name="dateFrom"
        value={value.dateFrom}
        onChange={(e) => onChange({ ...value, dateFrom: e.target.value })}
      />
      <Input
        type="date"
        label="Au"
        name="dateTo"
        value={value.dateTo}
        onChange={(e) => onChange({ ...value, dateTo: e.target.value })}
      />
    </div>
  );
}
