import { useState } from 'react';
import { useOutletContext, useSearchParams } from 'react-router-dom';
import { Card } from '@/shared/components/Card/Card';
import { Loader } from '@/shared/components/Loader/Loader';
import { Alert } from '@/shared/components/Alert/Alert';
import { Badge } from '@/shared/components/Badge/Badge';
import { Button } from '@/shared/components/Button/Button';
import { Select } from '@/shared/components/Select/Select';
import { FilterPanel } from '@/shared/components/FilterPanel/FilterPanel';
import { SortableColumnHeader } from '@/shared/components/SortableColumnHeader/SortableColumnHeader';
import { Pagination } from '@/shared/components/Pagination/Pagination';
import { EmptyState } from '@/shared/components/EmptyState/EmptyState';
import { getErrorMessage } from '@/shared/utils/getErrorMessage';
import { countActiveFilters } from '@/shared/utils/countActiveFilters';
import { nextSortDirection, type SortDirection } from '@/shared/utils/sorting';
import { useMembershipRequests } from '@/features/property-mngt/invitations/hooks/useMembershipRequests';
import { useAcceptMembershipRequest } from '@/features/property-mngt/invitations/hooks/useAcceptMembershipRequest';
import { RejectMembershipRequestModal } from '@/features/property-mngt/invitations/components/RejectMembershipRequestModal';
import type {
  MembershipRequest,
  MembershipRequestSortField,
  MembershipRequestStatus,
} from '@/features/property-mngt/invitations/types/invitation.types';
import type { Property } from '@/features/property-mngt/properties/types/property.types';

const STATUS_BADGE: Record<MembershipRequestStatus, { label: string; color: 'success' | 'warning' | 'error' }> = {
  PENDING: { label: 'En attente', color: 'warning' },
  ACCEPTED: { label: 'Acceptée', color: 'success' },
  REJECTED: { label: 'Refusée', color: 'error' },
};

// '' est le choix « aucun filtre » : la valeur d'un <select> est toujours une
// chaîne, le tri-état ne peut donc pas être un MembershipRequestStatus | undefined.
type StatusFilter = '' | MembershipRequestStatus;

function formatDate(value: string | null): string {
  return value ? new Date(value).toLocaleDateString('fr-FR') : '—';
}

/**
 * L'écran « Demandes d'adhésion », anciennement « Invitations ».
 *
 * <p>Il ne montre plus que des demandes d'adhésion. Le lien public a rejoint
 * « Informations générales » (voir PublicInvitationSection) et les invitations
 * nominatives se lancent depuis la fiche du contact concerné : trois objets
 * sans rapport cohabitaient ici, chacun avec son cycle de vie et ses actions,
 * et le syndic devait deviner lequel le concernait.
 *
 * <p>Tableau plutôt que liste de cartes, et charte commune aux autres tableaux
 * de l'application (FilterPanel + SortableColumnHeader) : une file d'attente
 * se trie et se filtre. Ordre par défaut : la plus récente en premier.
 */
export function MembershipRequestsTab() {
  const { property } = useOutletContext<{ property: Property }>();
  const [searchParams] = useSearchParams();
  // Posé par le lien d'une notification (voir MembershipRequestLinkComposer,
  // côté API) : la notification ouvre la liste sur la bonne ligne plutôt que
  // sur une page de détail pour un objet qui tient en cinq champs et dont les
  // deux seules actions sont déjà ici.
  const highlightedRequestId = searchParams.get('requestId');

  const [page, setPage] = useState(0);
  const [search, setSearch] = useState('');
  const [statusFilter, setStatusFilter] = useState<StatusFilter>('');
  const [sortBy, setSortBy] = useState<MembershipRequestSortField>('SUBMITTED_AT');
  const [sortDirection, setSortDirection] = useState<SortDirection>('DESC');
  const [requestToReject, setRequestToReject] = useState<MembershipRequest | null>(null);

  const isFiltered = search !== '' || statusFilter !== '';
  const requests = useMembershipRequests(property.id, page, {
    search: search || undefined,
    status: statusFilter || undefined,
    sortBy,
    sortDirection,
  });
  const acceptRequest = useAcceptMembershipRequest(property.id);

  function handleSort(field: MembershipRequestSortField) {
    setSortDirection(nextSortDirection(field, sortBy, sortDirection));
    setSortBy(field);
    setPage(0);
  }

  const content = requests.data?.content ?? [];

  return (
    <Card className="flex flex-col gap-4">
      <FilterPanel
        activeCount={countActiveFilters({ statusFilter, search }, { statusFilter: '', search: '' })}
        onClear={() => {
          setSearch('');
          setStatusFilter('');
          setPage(0);
        }}
        search={{
          value: search,
          onChange: (next) => {
            setSearch(next);
            setPage(0);
          },
          placeholder: 'Rechercher un demandeur, un email, un lot…',
        }}
      >
        <div className="grid grid-cols-1 gap-3 sm:grid-cols-2 lg:grid-cols-4">
          <Select
            label="Statut"
            // Input/Select dérivent le htmlFor du label de id ?? name : sans
            // l'un des deux, l'étiquette reste détachée du champ.
            name="status-filter"
            value={statusFilter}
            onChange={(e) => {
              setStatusFilter(e.target.value as StatusFilter);
              setPage(0);
            }}
          >
            <option value="">Tous les statuts</option>
            <option value="PENDING">En attente</option>
            <option value="ACCEPTED">Acceptée</option>
            <option value="REJECTED">Refusée</option>
          </Select>
        </div>
      </FilterPanel>

      {requests.isLoading && <Loader label="Chargement des demandes d'adhésion…" />}
      {requests.isError && <Alert message={getErrorMessage(requests.error)} />}
      {acceptRequest.isError && <Alert message={getErrorMessage(acceptRequest.error)} />}

      {requests.data && content.length === 0 && (
        <EmptyState title={isFiltered ? 'Aucune demande trouvée' : "Aucune demande d'adhésion pour le moment"}>
          {isFiltered
            ? 'Ajustez votre recherche ou vos filtres.'
            : "Les demandes déposées via le lien public de la copropriété apparaîtront ici."}
        </EmptyState>
      )}

      {requests.data && content.length > 0 && (
        <>
          <div className="overflow-x-auto rounded-lg border border-gray-200 dark:border-gray-800">
            <table className="min-w-full divide-y divide-gray-200 dark:divide-gray-800 text-sm">
              <thead>
                <tr className="text-left text-gray-500 dark:text-gray-400">
                  <SortableColumnHeader
                    field="REQUESTER"
                    activeField={sortBy}
                    direction={sortDirection}
                    onSort={handleSort}
                    className="px-3"
                  >
                    Demandeur
                  </SortableColumnHeader>
                  <SortableColumnHeader
                    field="UNIT"
                    activeField={sortBy}
                    direction={sortDirection}
                    onSort={handleSort}
                    className="px-3"
                  >
                    Lot
                  </SortableColumnHeader>
                  <SortableColumnHeader
                    field="SUBMITTED_AT"
                    activeField={sortBy}
                    direction={sortDirection}
                    onSort={handleSort}
                    className="px-3"
                  >
                    Déposée le
                  </SortableColumnHeader>
                  <SortableColumnHeader
                    field="STATUS"
                    activeField={sortBy}
                    direction={sortDirection}
                    onSort={handleSort}
                    className="px-3"
                  >
                    Statut
                  </SortableColumnHeader>
                  <th className="px-3 py-2 text-right font-medium">Actions</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-gray-100 dark:divide-gray-800">
                {content.map((request) => (
                  <tr
                    key={request.id}
                    // La ligne pointée par la notification se distingue, sans
                    // quoi le lecteur arrive sur un tableau de vingt lignes
                    // sans savoir laquelle l'a fait venir.
                    className={
                      request.id === highlightedRequestId
                        ? 'bg-brand-50 dark:bg-brand-500/10'
                        : 'hover:bg-gray-50 dark:hover:bg-white/[0.03]'
                    }
                  >
                    <td className="px-3 py-2">
                      <p className="font-medium text-gray-900 dark:text-white/90">
                        {request.requesterFullName ?? 'Compte supprimé'}
                      </p>
                      <p className="text-gray-500 dark:text-gray-400">{request.requesterEmail ?? '—'}</p>
                      {/* Une demande déposée pendant l'inscription arrive avant
                          que l'adresse ne soit confirmée : attribuer un lot à une
                          adresse jamais vérifiée est une décision, pas un détail. */}
                      {!request.requesterAccountVerified && (
                        <Badge color="warning">Email non vérifié</Badge>
                      )}
                    </td>
                    <td className="px-3 py-2 text-gray-500 dark:text-gray-400">
                      {request.unitNumber ?? '—'}
                      {request.unitTypeName && ` — ${request.unitTypeName}`}
                    </td>
                    <td className="px-3 py-2 text-gray-500 dark:text-gray-400">{formatDate(request.submittedAt)}</td>
                    <td className="px-3 py-2">
                      <Badge color={STATUS_BADGE[request.status].color}>{STATUS_BADGE[request.status].label}</Badge>
                      {request.status === 'REJECTED' && request.rejectionReason && (
                        <p className="text-xs text-gray-400 dark:text-gray-500">Motif : {request.rejectionReason}</p>
                      )}
                      {request.status !== 'PENDING' && request.decidedByFullName && (
                        <p className="text-xs text-gray-400 dark:text-gray-500">
                          Par {request.decidedByFullName} le {formatDate(request.decidedAt)}
                        </p>
                      )}
                    </td>
                    <td className="px-3 py-2">
                      {request.status === 'PENDING' && (
                        <div className="flex justify-end gap-2">
                          <Button
                            type="button"
                            className="min-h-0 px-3 py-1.5 text-xs"
                            isLoading={acceptRequest.isPending && acceptRequest.variables === request.id}
                            onClick={() => acceptRequest.mutate(request.id)}
                          >
                            Accepter
                          </Button>
                          <Button
                            type="button"
                            variant="secondary"
                            className="min-h-0 px-3 py-1.5 text-xs"
                            onClick={() => setRequestToReject(request)}
                          >
                            Refuser
                          </Button>
                        </div>
                      )}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
          <Pagination pageNumber={requests.data.pageNumber} totalPages={requests.data.totalPages} onPageChange={setPage} />
        </>
      )}

      <RejectMembershipRequestModal
        propertyId={property.id}
        request={requestToReject}
        onClose={() => setRequestToReject(null)}
      />
    </Card>
  );
}
