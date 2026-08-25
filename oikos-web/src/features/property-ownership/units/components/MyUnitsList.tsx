import { useMyUnits } from '@/features/property-ownership/units/hooks/useMyUnits';
import { useMyInstallments } from '@/features/property-ownership/installments/hooks/useMyInstallments';
import { MyUnitCard } from '@/features/property-ownership/units/components/MyUnitCard';
import { useMyPayments } from '@/features/property-ownership/payments/hooks/useMyPayments';
import { useMyMembershipRequests } from '@/features/property-ownership/membership-requests/hooks/useMyMembershipRequests';
import { PendingUnitCard } from '@/features/property-ownership/membership-requests/components/PendingUnitCard';
import { unitAccountBalance, unitDueCount } from '@/features/property-ownership/units/utils/unitBalance';
import { Loader } from '@/shared/components/Loader/Loader';
import { Alert } from '@/shared/components/Alert/Alert';
import { EmptyState } from '@/shared/components/EmptyState/EmptyState';
import { getErrorMessage } from '@/shared/utils/getErrorMessage';

/**
 * The owner's lots, each with the balance of its account - plus the lots whose
 * membership request the syndic has not decided yet, as non-clickable cards.
 *
 * Les deux tiennent dans la même liste parce que c'est la même question posée
 * par le copropriétaire : « où en sont mes lots ». Séparer les demandes en
 * attente dans un écran à part, c'est ce que faisait « Mes invitations » - une
 * page sans entrée de menu, sur laquelle on ne repassait jamais.
 *
 * Lives in a component rather than a page because the owner dashboard is now
 * its only host - "Mes lots" is no longer a screen of its own (see
 * privateRoutes: /property-ownership/units redirects to /dashboard).
 */
export function MyUnitsList() {
  const units = useMyUnits();
  // One request for every lot's echeances rather than one per card - the owner
  // endpoint already returns them all, and the per-lot figure is a filter away.
  const installments = useMyInstallments();
  // Même raison, et même cache que « Mes paiements » : le solde d'un lot est un
  // compte courant, il lui faut les deux côtés.
  const payments = useMyPayments();
  // Seules les demandes en attente : une demande refusée n'a plus rien à faire
  // ici (elle ne mènera à aucun lot), et une demande acceptée est déjà devenue
  // l'un des lots listés au-dessus.
  const membershipRequests = useMyMembershipRequests();
  const pendingRequests = (membershipRequests.data ?? []).filter((request) => request.status === 'PENDING');

  if (units.isLoading) {
    return <Loader label="Chargement de vos lots…" />;
  }

  if (units.isError) {
    return <Alert message={getErrorMessage(units.error)} />;
  }

  // Le vide n'est un vide que si les deux le sont : un compte qui vient
  // d'accepter une invitation publique n'a aucun lot validé, et lui annoncer
  // « vous n'êtes propriétaire d'aucun lot » contredirait la demande qu'il
  // vient précisément de déposer.
  if ((units.data ?? []).length === 0 && pendingRequests.length === 0) {
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
      {pendingRequests.map((request) => (
        <PendingUnitCard key={request.id} request={request} />
      ))}
    </div>
  );
}
