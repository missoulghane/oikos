import { useState } from 'react';
import { Link, useOutletContext, useParams } from 'react-router-dom';
import { useCurrentUser, canWriteAccounting } from '@/features/identity/me';
import { useLedgerAccounts } from '@/features/property-mngt/accounting/hooks/useLedgerAccounts';
import { useLedgerAccountEntries } from '@/features/property-mngt/accounting/hooks/useLedgerAccountEntries';
import { JournalEntryFilters, type JournalEntryFiltersValue } from '@/features/property-mngt/accounting/components/JournalEntryFilters';
import { JournalEntryRow } from '@/features/property-mngt/accounting/components/JournalEntryRow';
import { Card } from '@/shared/components/Card/Card';
import { Loader } from '@/shared/components/Loader/Loader';
import { Alert } from '@/shared/components/Alert/Alert';
import { EmptyState } from '@/shared/components/EmptyState/EmptyState';
import { Pagination } from '@/shared/components/Pagination/Pagination';
import { FilterPanel } from '@/shared/components/FilterPanel/FilterPanel';
import { getErrorMessage } from '@/shared/utils/getErrorMessage';
import { countActiveFilters } from '@/shared/utils/countActiveFilters';
import { ACCOUNT_ROLE_LABELS, getTreasuryBalanceColorClass } from '@/features/property-mngt/accounting/constants/accountingLabels';
import type { JournalEntryListFilters } from '@/features/property-mngt/accounting/types/accounting.types';
import type { Property } from '@/features/property-mngt/properties/types/property.types';

const DEFAULT_FILTERS: JournalEntryFiltersValue = {
  search: '',
  pieceDateFrom: '',
  pieceDateTo: '',
  status: '',
};

export function TreasuryAccountDetailPage() {
  const { property } = useOutletContext<{ property: Property }>();
  const { accountId } = useParams<{ accountId: string }>();
  const currentUser = useCurrentUser();
  const canWrite = currentUser.data ? canWriteAccounting(currentUser.data, property.id) : false;
  const ledgerAccounts = useLedgerAccounts(property.id);
  const account = ledgerAccounts.data?.find((a) => a.id === accountId);

  const [page, setPage] = useState(0);
  const [filters, setFilters] = useState<JournalEntryFiltersValue>(DEFAULT_FILTERS);

  function handleFiltersChange(next: JournalEntryFiltersValue) {
    setFilters(next);
    setPage(0);
  }

  const queryFilters: JournalEntryListFilters = {
    pieceDateFrom: filters.pieceDateFrom || undefined,
    pieceDateTo: filters.pieceDateTo || undefined,
    search: filters.search || undefined,
    status: filters.status || undefined,
  };

  const entries = useLedgerAccountEntries(property.id, accountId ?? '', page, queryFilters);

  return (
    <div className="flex flex-col gap-6">
      <div>
        <Link
          to={`/property-mngt/properties/${property.id}/accounting`}
          className="text-sm text-gray-500 dark:text-gray-400 hover:underline"
        >
          ← Retour à la vue d'ensemble
        </Link>
      </div>

      {ledgerAccounts.isLoading && <Loader label="Chargement du compte…" />}
      {ledgerAccounts.isError && <Alert message={getErrorMessage(ledgerAccounts.error)} />}
      {ledgerAccounts.data && !account && <EmptyState title="Compte introuvable" />}

      {account && (
        <Card className="flex flex-col gap-2">
          <p className="text-sm text-gray-500 dark:text-gray-400">{account.role ? ACCOUNT_ROLE_LABELS[account.role] : 'Compte'}</p>
          <h1 className="text-lg font-semibold text-gray-900 dark:text-white/90">
            {account.accountNumber} — {account.label}
          </h1>
          <p className={`text-2xl font-semibold ${getTreasuryBalanceColorClass(account.balance)}`}>
            {account.balance.toLocaleString('fr-FR')} MAD
          </p>
        </Card>
      )}

      <Card className="flex flex-col gap-4">
        <div className="flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between">
          <div>
            <h2 className="text-base font-semibold text-gray-900 dark:text-white/90">Opérations</h2>
            <p className="text-sm text-gray-500 dark:text-gray-400">Écritures ayant mouvementé ce compte.</p>
          </div>
          {canWrite && (
            <div className="flex flex-wrap gap-3">
              <Link
                to={`/property-mngt/properties/${property.id}/accounting/treasury-accounts/${accountId}/expenses/new`}
                className="inline-flex min-h-11 items-center rounded-lg bg-brand-500 px-4 py-2 text-sm font-medium text-white shadow-theme-xs hover:bg-brand-600"
              >
                Saisir une dépense
              </Link>
              <Link
                to={`/property-mngt/properties/${property.id}/accounting/treasury-accounts/${accountId}/receipts/new`}
                className="inline-flex min-h-11 items-center rounded-lg bg-white dark:bg-gray-800 px-4 py-2 text-sm font-medium text-gray-700 dark:text-gray-400 shadow-theme-xs ring-1 ring-inset ring-gray-300 dark:ring-gray-700 hover:bg-gray-50 dark:hover:bg-white/[0.03]"
              >
                Saisir une recette
              </Link>
              <Link
                to={`/property-mngt/properties/${property.id}/accounting/treasury-accounts/${accountId}/transfers/new`}
                className="inline-flex min-h-11 items-center rounded-lg bg-white dark:bg-gray-800 px-4 py-2 text-sm font-medium text-gray-700 dark:text-gray-400 shadow-theme-xs ring-1 ring-inset ring-gray-300 dark:ring-gray-700 hover:bg-gray-50 dark:hover:bg-white/[0.03]"
              >
                Virement entre comptes
              </Link>
            </div>
          )}
        </div>

        {/* No sort keys to exclude here: every field of this bar is a real filter. */}
        <FilterPanel
          activeCount={countActiveFilters(filters, DEFAULT_FILTERS)}
          onClear={() => handleFiltersChange(DEFAULT_FILTERS)}
        >
          <JournalEntryFilters value={filters} onChange={handleFiltersChange} />
        </FilterPanel>

        {entries.isLoading && <Loader label="Chargement des opérations…" />}
        {entries.isError && <Alert message={getErrorMessage(entries.error)} />}
        {entries.data && entries.data.content.length === 0 && (
          <EmptyState title="Aucune opération">Aucune opération ne correspond à ces critères.</EmptyState>
        )}
        {entries.data && entries.data.content.length > 0 && (
          <div className="flex flex-col gap-3">
            <ul className="flex flex-col divide-y divide-gray-200 dark:divide-gray-800 rounded-lg border border-gray-200 dark:border-gray-800">
              {entries.data.content.map((entry) => (
                <JournalEntryRow key={entry.id} entry={entry} propertyId={property.id} />
              ))}
            </ul>
            <Pagination
              pageNumber={entries.data.pageNumber}
              totalPages={entries.data.totalPages}
              onPageChange={setPage}
            />
          </div>
        )}
      </Card>
    </div>
  );
}
