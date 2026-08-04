import { useState } from 'react';
import { Link } from 'react-router-dom';
import { useParties } from '@/features/property-mngt/parties/hooks/useParties';
import { useProperties } from '@/features/property-mngt/properties/hooks/useProperties';
import { PARTY_TYPE_LABELS } from '@/features/property-mngt/properties/constants/partyTypeLabels';
import { Input } from '@/shared/components/Input/Input';
import { Select } from '@/shared/components/Select/Select';
import { Loader } from '@/shared/components/Loader/Loader';
import { Alert } from '@/shared/components/Alert/Alert';
import { EmptyState } from '@/shared/components/EmptyState/EmptyState';
import { Pagination } from '@/shared/components/Pagination/Pagination';
import { getErrorMessage } from '@/shared/utils/getErrorMessage';

const PROPERTY_PICKER_SIZE = 100;

export function PartiesPage() {
  const [propertyId, setPropertyId] = useState('');
  const [page, setPage] = useState(0);
  const [search, setSearch] = useState('');
  const properties = useProperties(0, PROPERTY_PICKER_SIZE);
  const parties = useParties(propertyId || undefined, page, search || undefined);

  return (
    <div className="flex flex-col gap-6">
      <h1 className="text-lg font-semibold text-gray-900">Contacts</h1>
      <Select
        label="Copropriété"
        value={propertyId}
        onChange={(e) => {
          setPropertyId(e.target.value);
          setPage(0);
        }}
      >
        <option value="">Sélectionnez une copropriété</option>
        {properties.data?.content.map((property) => (
          <option key={property.id} value={property.id}>
            {property.name}
          </option>
        ))}
      </Select>

      {propertyId && (
        <Input
          label="Rechercher (nom, email)"
          value={search}
          onChange={(e) => {
            setSearch(e.target.value);
            setPage(0);
          }}
        />
      )}

      {!propertyId && (
        <EmptyState title="Choisissez une copropriété">
          Sélectionnez une copropriété ci-dessus pour afficher ses contacts.
        </EmptyState>
      )}
      {propertyId && parties.isLoading && <Loader label="Chargement des contacts…" />}
      {propertyId && parties.isError && <Alert message={getErrorMessage(parties.error)} />}
      {propertyId && parties.data && parties.data.content.length === 0 && (
        <EmptyState title="Aucun contact trouvé">Ajustez votre recherche ou créez un propriétaire depuis un lot.</EmptyState>
      )}
      {propertyId && parties.data && parties.data.content.length > 0 && (
        <div className="flex flex-col gap-3">
          <ul className="flex flex-col divide-y divide-gray-200 rounded-lg border border-gray-200">
            {parties.data.content.map((party) => (
              <li key={party.id}>
                <Link
                  to={`/parties/${propertyId}/${party.id}`}
                  className="flex flex-col gap-1 px-3 py-2 hover:bg-gray-50 sm:flex-row sm:items-center sm:justify-between"
                >
                  <div>
                    <p className="text-sm font-medium text-gray-900">
                      {party.fullName} ({PARTY_TYPE_LABELS[party.partyType]})
                    </p>
                    <p className="text-sm text-gray-500">{party.email}</p>
                  </div>
                  {party.phone && <span className="text-sm text-gray-500">{party.phone}</span>}
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
