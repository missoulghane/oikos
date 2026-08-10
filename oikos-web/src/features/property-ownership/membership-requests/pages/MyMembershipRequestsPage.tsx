import { useMyMembershipRequests } from '@/features/property-ownership/membership-requests/hooks/useMyMembershipRequests';
import { Card } from '@/shared/components/Card/Card';
import { Loader } from '@/shared/components/Loader/Loader';
import { Alert } from '@/shared/components/Alert/Alert';
import { Badge } from '@/shared/components/Badge/Badge';
import { EmptyState } from '@/shared/components/EmptyState/EmptyState';
import { getErrorMessage } from '@/shared/utils/getErrorMessage';
import type { OwnedMembershipRequestStatus } from '@/features/property-ownership/membership-requests/types/membershipRequest.types';

const STATUS_BADGE: Record<OwnedMembershipRequestStatus, { label: string; color: 'success' | 'warning' | 'error' }> = {
  PENDING: { label: 'En attente', color: 'warning' },
  ACCEPTED: { label: 'Acceptée', color: 'success' },
  REJECTED: { label: 'Refusée', color: 'error' },
};

export function MyMembershipRequestsPage() {
  const requests = useMyMembershipRequests();

  return (
    <div className="flex flex-col gap-6">
      <h1 className="text-lg font-semibold text-gray-900 dark:text-white/90">Mes invitations</h1>

      <Card className="flex flex-col gap-2">
        {requests.isLoading && <Loader label="Chargement de vos demandes…" />}
        {requests.isError && <Alert message={getErrorMessage(requests.error)} />}
        {requests.data && requests.data.length === 0 && (
          <EmptyState title="Aucune demande d'adhésion">
            Vos demandes d'adhésion soumises via un lien d'invitation apparaîtront ici.
          </EmptyState>
        )}
        {requests.data && requests.data.length > 0 && (
          <ul className="flex flex-col divide-y divide-gray-100 dark:divide-gray-800">
            {requests.data.map((request) => {
              const badge = STATUS_BADGE[request.status];
              return (
                <li key={request.id} className="flex flex-col gap-1 py-2 text-sm">
                  <div className="flex items-center justify-between gap-2">
                    <p className="text-gray-700 dark:text-gray-300">
                      {request.propertyName ?? 'Copropriété'}
                      {request.unitNumber && ` — Lot ${request.unitNumber}`}
                      {request.unitTypeName && ` (${request.unitTypeName})`}
                    </p>
                    <Badge color={badge.color}>{badge.label}</Badge>
                  </div>
                  {request.status === 'REJECTED' && request.rejectionReason && (
                    <p className="text-xs text-gray-400 dark:text-gray-500">Motif : {request.rejectionReason}</p>
                  )}
                </li>
              );
            })}
          </ul>
        )}
      </Card>
    </div>
  );
}
