import { Card } from '@/shared/components/Card/Card';
import { Badge } from '@/shared/components/Badge/Badge';
import type { OwnedMembershipRequest } from '@/features/property-ownership/membership-requests/types/membershipRequest.types';

interface PendingUnitCardProps {
  request: OwnedMembershipRequest;
}

/**
 * Un lot demandé via un lien d'invitation, que le syndic n'a pas encore validé.
 *
 * Volontairement une Card et non une CardLink, à la différence de MyUnitCard :
 * il n'y a pas encore de fiche derrière, ni compte, ni échéance - le rôle
 * n'est posé qu'à la validation. Une carte cliquable mènerait à un 403.
 * Le badge dit l'état, la phrase dit ce qu'il faut en attendre : sans elle,
 * « En attente de validation » laisse le copropriétaire chercher ce qu'il a
 * encore à faire, alors qu'il n'a rien à faire.
 */
export function PendingUnitCard({ request }: PendingUnitCardProps) {
  return (
    <Card className="flex flex-col gap-3">
      <div className="flex items-start justify-between gap-3">
        <div className="flex flex-col gap-1">
          <h2 className="font-medium text-gray-900 dark:text-white/90">{request.propertyName ?? 'Copropriété'}</h2>
          <p className="text-sm text-gray-500 dark:text-gray-400">
            {request.unitNumber ?? 'Lot'}
            {request.unitTypeName && ` — ${request.unitTypeName}`}
          </p>
        </div>
        <Badge color="warning">En attente de validation</Badge>
      </div>
      <p className="text-sm text-gray-500 dark:text-gray-400">
        Une fois votre demande validée, vous recevrez une notification et la gestion de votre lot sera entièrement à
        vous ! 🎉
      </p>
    </Card>
  );
}
