import { useMyUnits } from '@/features/property-ownership/units/hooks/useMyUnits';
import { useMyInstallments } from '@/features/property-ownership/installments/hooks/useMyInstallments';
import { MyUnitCard } from '@/features/property-ownership/units/components/MyUnitCard';
import { unitOutstanding } from '@/features/property-ownership/units/utils/unitBalance';
import { Loader } from '@/shared/components/Loader/Loader';
import { Alert } from '@/shared/components/Alert/Alert';
import { EmptyState } from '@/shared/components/EmptyState/EmptyState';
import { getErrorMessage } from '@/shared/utils/getErrorMessage';

/**
 * The owner's lots, each with what it still owes. Lives in a component rather
 * than a page because the owner dashboard is now its only host - "Mes lots" is
 * no longer a screen of its own (see privateRoutes: /property-ownership/units
 * redirects to /dashboard).
 */
export function MyUnitsList() {
  const units = useMyUnits();
  // One request for every lot's echeances rather than one per card - the owner
  // endpoint already returns them all, and the per-lot figure is a filter away.
  const installments = useMyInstallments();

  if (units.isLoading) {
    return <Loader label="Chargement de vos lots…" />;
  }

  if (units.isError) {
    return <Alert message={getErrorMessage(units.error)} />;
  }

  if ((units.data ?? []).length === 0) {
    return <EmptyState title="Aucun lot">Vous n'êtes propriétaire d'aucun lot pour le moment.</EmptyState>;
  }

  return (
    <div className="grid grid-cols-1 gap-4 sm:grid-cols-2 lg:grid-cols-3">
      {(units.data ?? []).map((unit) => (
        <MyUnitCard
          key={unit.unitId}
          unit={unit}
          outstanding={installments.data ? unitOutstanding(installments.data, unit.unitId) : undefined}
        />
      ))}
    </div>
  );
}
