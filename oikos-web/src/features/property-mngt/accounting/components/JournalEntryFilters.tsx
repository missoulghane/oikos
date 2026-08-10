import { Select } from '@/shared/components/Select/Select';
import { Input } from '@/shared/components/Input/Input';
import { JOURNAL_ENTRY_STATUS_LABELS } from '@/features/property-mngt/accounting/constants/accountingLabels';
import type { JournalEntryStatus } from '@/features/property-mngt/accounting/types/accounting.types';

export interface JournalEntryFiltersValue {
  search: string;
  pieceDateFrom: string;
  pieceDateTo: string;
  status: JournalEntryStatus | '';
}

interface JournalEntryFiltersProps {
  value: JournalEntryFiltersValue;
  onChange: (value: JournalEntryFiltersValue) => void;
}

export function JournalEntryFilters({ value, onChange }: JournalEntryFiltersProps) {
  return (
    <div className="grid grid-cols-1 gap-3 sm:grid-cols-2 lg:grid-cols-4">
      <Input
        label="Recherche"
        placeholder="Référence…"
        value={value.search}
        onChange={(e) => onChange({ ...value, search: e.target.value })}
      />
      <Input
        type="date"
        label="Du"
        value={value.pieceDateFrom}
        onChange={(e) => onChange({ ...value, pieceDateFrom: e.target.value })}
      />
      <Input
        type="date"
        label="Au"
        value={value.pieceDateTo}
        onChange={(e) => onChange({ ...value, pieceDateTo: e.target.value })}
      />
      <Select
        label="Statut"
        value={value.status}
        onChange={(e) => onChange({ ...value, status: e.target.value as JournalEntryStatus | '' })}
      >
        <option value="">Tous</option>
        {(Object.keys(JOURNAL_ENTRY_STATUS_LABELS) as JournalEntryStatus[]).map((status) => (
          <option key={status} value={status}>
            {JOURNAL_ENTRY_STATUS_LABELS[status]}
          </option>
        ))}
      </Select>
    </div>
  );
}
