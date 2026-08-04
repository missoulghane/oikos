import { useState } from 'react';
import { useOutletContext } from 'react-router-dom';
import { useFinancialAccounts } from '@/features/property-mngt/accounting/hooks/useFinancialAccounts';
import { useFinancialJournal } from '@/features/property-mngt/accounting/hooks/useFinancialJournal';
import { JournalFilters, type JournalFiltersValue } from '@/features/property-mngt/accounting/components/JournalFilters';
import { JournalEntryRow } from '@/features/property-mngt/accounting/components/JournalEntryRow';
import { Card } from '@/shared/components/Card/Card';
import { Loader } from '@/shared/components/Loader/Loader';
import { Alert } from '@/shared/components/Alert/Alert';
import { EmptyState } from '@/shared/components/EmptyState/EmptyState';
import { Pagination } from '@/shared/components/Pagination/Pagination';
import { getErrorMessage } from '@/shared/utils/getErrorMessage';
import type { JournalFilters as JournalFiltersType } from '@/features/property-mngt/accounting/types/accounting.types';
import type { Property } from '@/features/property-mngt/properties/types/property.types';

const DEFAULT_FILTERS: JournalFiltersValue = { financialAccountId: '', type: '', dateFrom: '', dateTo: '' };

export function AccountingJournalTab() {
  const { property } = useOutletContext<{ property: Property }>();
  const [page, setPage] = useState(0);
  const [filters, setFilters] = useState<JournalFiltersValue>(DEFAULT_FILTERS);

  function handleFiltersChange(next: JournalFiltersValue) {
    setFilters(next);
    setPage(0);
  }

  const queryFilters: JournalFiltersType = {
    financialAccountId: filters.financialAccountId || undefined,
    type: filters.type || undefined,
    dateFrom: filters.dateFrom || undefined,
    dateTo: filters.dateTo || undefined,
  };

  const financialAccounts = useFinancialAccounts(property.id);
  const journal = useFinancialJournal(property.id, page, queryFilters);
  const accountNameById = new Map((financialAccounts.data ?? []).map((account) => [account.id, account.name]));

  return (
    <Card className="flex flex-col gap-4">
      <JournalFilters value={filters} onChange={handleFiltersChange} accounts={financialAccounts.data ?? []} />

      {journal.isLoading && <Loader label="Chargement du journal…" />}
      {journal.isError && <Alert message={getErrorMessage(journal.error)} />}
      {journal.data && journal.data.content.length === 0 && (
        <EmptyState title="Aucune écriture">Aucune écriture ne correspond à ces critères.</EmptyState>
      )}
      {journal.data && journal.data.content.length > 0 && (
        <div className="flex flex-col gap-3">
          <ul className="flex flex-col divide-y divide-gray-200 rounded-lg border border-gray-200">
            {journal.data.content.map((entry) => (
              <JournalEntryRow
                key={entry.id}
                entry={entry}
                accountName={accountNameById.get(entry.financialAccountId) ?? 'Compte inconnu'}
              />
            ))}
          </ul>
          <Pagination pageNumber={journal.data.pageNumber} totalPages={journal.data.totalPages} onPageChange={setPage} />
        </div>
      )}
    </Card>
  );
}
