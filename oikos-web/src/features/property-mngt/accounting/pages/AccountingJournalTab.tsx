import { useState } from 'react';
import { useOutletContext } from 'react-router-dom';
import { useJournalEntries } from '@/features/property-mngt/accounting/hooks/useJournalEntries';
import { JournalEntryRow } from '@/features/property-mngt/accounting/components/JournalEntryRow';
import { Card } from '@/shared/components/Card/Card';
import { Loader } from '@/shared/components/Loader/Loader';
import { Alert } from '@/shared/components/Alert/Alert';
import { EmptyState } from '@/shared/components/EmptyState/EmptyState';
import { Pagination } from '@/shared/components/Pagination/Pagination';
import { getErrorMessage } from '@/shared/utils/getErrorMessage';
import type { Property } from '@/features/property-mngt/properties/types/property.types';

export function AccountingJournalTab() {
  const { property } = useOutletContext<{ property: Property }>();
  const [page, setPage] = useState(0);
  const journalEntries = useJournalEntries(property.id, page);

  return (
    <Card className="flex flex-col gap-4">
      <div>
        <h2 className="text-base font-semibold text-gray-900 dark:text-white/90">Journal des écritures</h2>
        <p className="text-sm text-gray-500 dark:text-gray-400">Toutes les écritures comptables générées pour cette copropriété.</p>
      </div>

      {journalEntries.isLoading && <Loader label="Chargement du journal…" />}
      {journalEntries.isError && <Alert message={getErrorMessage(journalEntries.error)} />}
      {journalEntries.data && journalEntries.data.content.length === 0 && (
        <EmptyState title="Aucune écriture pour le moment" />
      )}
      {journalEntries.data && journalEntries.data.content.length > 0 && (
        <div className="flex flex-col gap-3">
          <ul className="flex flex-col divide-y divide-gray-200 dark:divide-gray-800 rounded-lg border border-gray-200 dark:border-gray-800">
            {journalEntries.data.content.map((entry) => (
              <JournalEntryRow key={entry.id} entry={entry} propertyId={property.id} />
            ))}
          </ul>
          <Pagination
            pageNumber={journalEntries.data.pageNumber}
            totalPages={journalEntries.data.totalPages}
            onPageChange={setPage}
          />
        </div>
      )}
    </Card>
  );
}
