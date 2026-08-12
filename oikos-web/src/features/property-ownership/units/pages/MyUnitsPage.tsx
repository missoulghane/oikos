import { useMyUnits } from '@/features/property-ownership/units/hooks/useMyUnits';
import { MyUnitCard } from '@/features/property-ownership/units/components/MyUnitCard';
import { Loader } from '@/shared/components/Loader/Loader';
import { Alert } from '@/shared/components/Alert/Alert';
import { EmptyState } from '@/shared/components/EmptyState/EmptyState';
import { getErrorMessage } from '@/shared/utils/getErrorMessage';

export function MyUnitsPage() {
  const units = useMyUnits();

  return (
    <div className="flex flex-col gap-6">
      <h1 className="text-lg font-semibold text-gray-900 dark:text-white/90">Mes lots</h1>

      {units.isLoading && <Loader label="Chargement de vos lots…" />}
      {units.isError && <Alert message={getErrorMessage(units.error)} />}
      {units.data && units.data.length === 0 && (
        <EmptyState title="Aucun lot">Vous n'êtes propriétaire d'aucun lot pour le moment.</EmptyState>
      )}
      {units.data && units.data.length > 0 && (
        <div className="grid grid-cols-1 gap-4 sm:grid-cols-2 lg:grid-cols-3">
          {units.data.map((unit) => (
            <MyUnitCard key={unit.unitId} unit={unit} />
          ))}
        </div>
      )}
    </div>
  );
}
