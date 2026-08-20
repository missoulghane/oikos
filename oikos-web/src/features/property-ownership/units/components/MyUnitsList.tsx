import { useMyUnits } from '@/features/property-ownership/units/hooks/useMyUnits';
import { useMyInstallments } from '@/features/property-ownership/installments/hooks/useMyInstallments';
import { MyUnitCard } from '@/features/property-ownership/units/components/MyUnitCard';
import { useMyPayments } from '@/features/property-ownership/payments/hooks/useMyPayments';
import { unitAccountBalance, unitDueCount } from '@/features/property-ownership/units/utils/unitBalance';
import { Loader } from '@/shared/components/Loader/Loader';
import { Alert } from '@/shared/components/Alert/Alert';
import { EmptyState } from '@/shared/components/EmptyState/EmptyState';
import { getErrorMessage } from '@/shared/utils/getErrorMessage';

/**
 * The owner's lots, each with the balance of its account. Lives in a component
 * rather than a page because the owner dashboard is now its only host - "Mes
 * lots" is no longer a screen of its own (see privateRoutes:
 * /property-ownership/units redirects to /dashboard).
 */
export function MyUnitsList() {
  const units = useMyUnits();
  // One request for every lot's echeances rather than one per card - the owner
  // endpoint already returns them all, and the per-lot figure is a filter away.
  const installments = useMyInstallments();
  // Même raison, et même cache que « Mes paiements » : le solde d'un lot est un
  // compte courant, il lui faut les deux côtés.
  const payments = useMyPayments();

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
    // Une carte par ligne, sur toute la largeur, plutôt qu'une grille de trois :
    // le solde est le chiffre que le copropriétaire vient chercher, et un
    // chiffre coincé dans un tiers de page se lit comme une vignette parmi
    // d'autres. Un lot par ligne le donne à la même échelle que sa fiche.
    <div className="flex flex-col gap-4">
      {(units.data ?? []).map((unit) => {
        // Les deux côtés du compte, ou rien : afficher les appels sans les
        // versements donnerait un solde franchement débiteur le temps que la
        // seconde requête arrive.
        const called = installments.data;
        const paid = payments.data;
        return (
          <MyUnitCard
            key={unit.unitId}
            unit={unit}
            balance={called && paid ? unitAccountBalance(called, paid, unit.unitId) : undefined}
            dueCount={called ? unitDueCount(called, unit.unitId) : undefined}
          />
        );
      })}
    </div>
  );
}
