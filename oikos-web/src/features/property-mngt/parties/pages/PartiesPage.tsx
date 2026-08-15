import { useState } from 'react';
import { Link } from 'react-router-dom';
import { useParties } from '@/features/property-mngt/parties/hooks/useParties';
import { useProperties } from '@/features/property-mngt/properties/hooks/useProperties';
import { CreatePartyForm } from '@/features/property-mngt/parties/components/CreatePartyForm';
import { PARTY_TYPE_LABELS } from '@/features/property-mngt/properties/constants/partyTypeLabels';
import { Select } from '@/shared/components/Select/Select';
import { FilterPanel } from '@/shared/components/FilterPanel/FilterPanel';
import { SortableColumnHeader } from '@/shared/components/SortableColumnHeader/SortableColumnHeader';
import { Button } from '@/shared/components/Button/Button';
import { Loader } from '@/shared/components/Loader/Loader';
import { Alert } from '@/shared/components/Alert/Alert';
import { EmptyState } from '@/shared/components/EmptyState/EmptyState';
import { Pagination } from '@/shared/components/Pagination/Pagination';
import { getErrorMessage } from '@/shared/utils/getErrorMessage';
import { countActiveFilters } from '@/shared/utils/countActiveFilters';
import { nextSortDirection, type SortDirection } from '@/shared/utils/sorting';
import type { PartySortField } from '@/features/property-mngt/parties/types/party.types';

const PROPERTY_PICKER_SIZE = 100;

export function PartiesPage() {
  const [propertyId, setPropertyId] = useState('');
  const [page, setPage] = useState(0);
  const [search, setSearch] = useState('');
  const [isCreating, setIsCreating] = useState(false);
  const properties = useProperties(0, PROPERTY_PICKER_SIZE);
  // Name ascending: the order the list already came in before it became sortable.
  const [sortBy, setSortBy] = useState<PartySortField>('FULL_NAME');
  const [sortDirection, setSortDirection] = useState<SortDirection>('ASC');
  const parties = useParties(propertyId || undefined, page, {
    search: search || undefined,
    sortBy,
    sortDirection,
  });

  function handleSort(field: PartySortField) {
    setSortDirection(nextSortDirection(field, sortBy, sortDirection));
    setSortBy(field);
    setPage(0);
  }

  return (
    <div className="flex flex-col gap-6">
      <div className="flex items-center justify-between">
        <h1 className="text-lg font-semibold text-gray-900 dark:text-white/90">Contacts</h1>
        {propertyId && !isCreating && (
          <Button type="button" onClick={() => setIsCreating(true)}>
            Nouveau contact
          </Button>
        )}
      </div>
      <Select
        label="Copropriété"
        value={propertyId}
        onChange={(e) => {
          setPropertyId(e.target.value);
          setPage(0);
          setIsCreating(false);
        }}
      >
        <option value="">Sélectionnez une copropriété</option>
        {properties.data?.content.map((property) => (
          <option key={property.id} value={property.id}>
            {property.name}
          </option>
        ))}
      </Select>

      {propertyId && isCreating && (
        <CreatePartyForm
          propertyId={propertyId}
          onSuccess={() => setIsCreating(false)}
          onCancel={() => setIsCreating(false)}
        />
      )}

      {propertyId && (
        <FilterPanel
          activeCount={countActiveFilters({ search }, { search: '' })}
          onClear={() => {
            setSearch('');
            setPage(0);
          }}
          search={{
            value: search,
            onChange: (next) => {
              setSearch(next);
              setPage(0);
            },
            placeholder: 'Rechercher un nom, un email…',
          }}
        >
          {/* No other criterion on this list yet - the panel keeps the toolbar
              identical to every other list, and holds the fields to come. */}
          <p className="text-sm text-gray-500 dark:text-gray-400">
            Aucun filtre supplémentaire sur cette liste.
          </p>
        </FilterPanel>
      )}

      {!propertyId && (
        <EmptyState title="Choisissez une copropriété">
          Sélectionnez une copropriété ci-dessus pour afficher ses contacts.
        </EmptyState>
      )}
      {propertyId && parties.isLoading && <Loader label="Chargement des contacts…" />}
      {propertyId && parties.isError && <Alert message={getErrorMessage(parties.error)} />}
      {propertyId && parties.data && parties.data.content.length === 0 && (
        <EmptyState title="Aucun contact trouvé">
          Ajustez votre recherche, créez un nouveau contact ci-dessus, ou un propriétaire depuis un lot.
        </EmptyState>
      )}
      {propertyId && parties.data && parties.data.content.length > 0 && (
        <div className="flex flex-col gap-3">
          <div className="overflow-x-auto rounded-lg border border-gray-200 dark:border-gray-800">
            <table className="min-w-full divide-y divide-gray-200 dark:divide-gray-800 text-sm">
              <thead>
                <tr className="text-left text-gray-500 dark:text-gray-400">
                  <SortableColumnHeader
                    field="FULL_NAME"
                    activeField={sortBy}
                    direction={sortDirection}
                    onSort={handleSort}
                    className="px-3"
                  >
                    Contact
                  </SortableColumnHeader>
                  <SortableColumnHeader
                    field="EMAIL"
                    activeField={sortBy}
                    direction={sortDirection}
                    onSort={handleSort}
                    className="px-3"
                  >
                    Email
                  </SortableColumnHeader>
                  <th className="px-3 py-2 font-medium">Téléphone</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-gray-100 dark:divide-gray-800">
                {parties.data.content.map((party) => (
                  <tr key={party.id} className="hover:bg-gray-50 dark:hover:bg-white/[0.03]">
                    <td className="px-3 py-2">
                      <Link
                        to={`/parties/${propertyId}/${party.id}`}
                        className="font-medium text-gray-900 hover:underline dark:text-white/90"
                      >
                        {party.fullName}
                      </Link>{' '}
                      <span className="text-gray-500 dark:text-gray-400">({PARTY_TYPE_LABELS[party.partyType]})</span>
                    </td>
                    <td className="px-3 py-2 text-gray-500 dark:text-gray-400">{party.email}</td>
                    <td className="px-3 py-2 text-gray-500 dark:text-gray-400">{party.phone ?? '—'}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
          <Pagination pageNumber={parties.data.pageNumber} totalPages={parties.data.totalPages} onPageChange={setPage} />
        </div>
      )}
    </div>
  );
}
