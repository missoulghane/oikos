import { useState } from 'react';
import { Link } from 'react-router-dom';
import { useProperties } from '@/features/properties/hooks/useProperties';
import { PropertyCard } from '@/features/properties/components/PropertyCard';
import { Loader } from '@/shared/components/Loader/Loader';
import { EmptyState } from '@/shared/components/EmptyState/EmptyState';
import { Alert } from '@/shared/components/Alert/Alert';
import { Pagination } from '@/shared/components/Pagination/Pagination';
import { getErrorMessage } from '@/shared/utils/getErrorMessage';

export function PropertyList() {
  const [page, setPage] = useState(0);
  const { data, isLoading, isError, error } = useProperties(page);

  if (isLoading) {
    return <Loader label="Chargement des copropriétés…" />;
  }

  if (isError) {
    return <Alert message={getErrorMessage(error)} />;
  }

  if (!data || data.content.length === 0) {
    return (
      <EmptyState
        title="Aucune copropriété pour le moment"
        action={
          <Link
            to="/properties/new"
            className="min-h-11 rounded-md bg-slate-900 px-4 py-2 text-sm font-medium text-white hover:bg-slate-700"
          >
            Créer une copropriété
          </Link>
        }
      >
        Commencez par créer votre première copropriété.
      </EmptyState>
    );
  }

  return (
    <div className="flex flex-col gap-4">
      <div className="grid grid-cols-1 gap-4 sm:grid-cols-2">
        {data.content.map((property) => (
          <PropertyCard key={property.id} property={property} />
        ))}
      </div>
      <Pagination pageNumber={data.pageNumber} totalPages={data.totalPages} onPageChange={setPage} />
    </div>
  );
}
