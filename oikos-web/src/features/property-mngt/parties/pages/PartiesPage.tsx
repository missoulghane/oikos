import { useState } from 'react';
import { Link } from 'react-router-dom';
import { useParties } from '@/features/property-mngt/parties/hooks/useParties';
import { PARTY_TYPE_LABELS } from '@/features/property-mngt/properties/constants/partyTypeLabels';
import { Input } from '@/shared/components/Input/Input';
import { Loader } from '@/shared/components/Loader/Loader';
import { Alert } from '@/shared/components/Alert/Alert';
import { EmptyState } from '@/shared/components/EmptyState/EmptyState';
import { Pagination } from '@/shared/components/Pagination/Pagination';
import { getErrorMessage } from '@/shared/utils/getErrorMessage';

export function PartiesPage() {
  const [page, setPage] = useState(0);
  const [search, setSearch] = useState('');
  const parties = useParties(page, search || undefined);

  return (
    <div className="flex flex-col gap-6">
      <h1 className="text-lg font-semibold text-slate-900">Contacts</h1>
      <Input
        label="Rechercher (nom, email)"
        value={search}
        onChange={(e) => {
          setSearch(e.target.value);
          setPage(0);
        }}
      />

      {parties.isLoading && <Loader label="Chargement des contacts…" />}
      {parties.isError && <Alert message={getErrorMessage(parties.error)} />}
      {parties.data && parties.data.content.length === 0 && (
        <EmptyState title="Aucun contact trouvé">Ajustez votre recherche ou créez un propriétaire depuis un lot.</EmptyState>
      )}
      {parties.data && parties.data.content.length > 0 && (
        <div className="flex flex-col gap-3">
          <ul className="flex flex-col divide-y divide-slate-200 rounded-md border border-slate-200">
            {parties.data.content.map((party) => (
              <li key={party.id}>
                <Link
                  to={`/parties/${party.id}`}
                  className="flex flex-col gap-1 px-3 py-2 hover:bg-slate-50 sm:flex-row sm:items-center sm:justify-between"
                >
                  <div>
                    <p className="text-sm font-medium text-slate-900">
                      {party.fullName} ({PARTY_TYPE_LABELS[party.partyType]})
                    </p>
                    <p className="text-sm text-slate-500">{party.email}</p>
                  </div>
                  {party.phone && <span className="text-sm text-slate-500">{party.phone}</span>}
                </Link>
              </li>
            ))}
          </ul>
          <Pagination pageNumber={parties.data.pageNumber} totalPages={parties.data.totalPages} onPageChange={setPage} />
        </div>
      )}
    </div>
  );
}
