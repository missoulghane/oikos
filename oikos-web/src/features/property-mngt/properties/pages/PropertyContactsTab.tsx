import { useMemo, useState } from 'react';
import { useOutletContext } from 'react-router-dom';
import { Link } from 'react-router-dom';
import { usePropertyContacts } from '@/features/property-mngt/properties/hooks/usePropertyContacts';
import { PARTY_TYPE_LABELS } from '@/features/property-mngt/properties/constants/partyTypeLabels';
import { useBulkInviteParties } from '@/features/property-mngt/parties/hooks/useBulkInviteParties';
import { Card } from '@/shared/components/Card/Card';
import { Loader } from '@/shared/components/Loader/Loader';
import { Alert } from '@/shared/components/Alert/Alert';
import { Input } from '@/shared/components/Input/Input';
import { Button } from '@/shared/components/Button/Button';
import { Pagination } from '@/shared/components/Pagination/Pagination';
import { EmptyState } from '@/shared/components/EmptyState/EmptyState';
import { getErrorMessage } from '@/shared/utils/getErrorMessage';
import { CheckCircleIcon } from '@/shared/icons';
import type { Property, PropertyContact } from '@/features/property-mngt/properties/types/property.types';

interface ContactGroup {
  partyId: string;
  partyFullName: string;
  partyType: PropertyContact['partyType'];
  partyPhone: string | null;
  hasLinkedAccount: boolean;
  units: PropertyContact[];
}

function groupByParty(contacts: PropertyContact[]): ContactGroup[] {
  const groups = new Map<string, ContactGroup>();
  for (const contact of contacts) {
    const existing = groups.get(contact.partyId);
    if (existing) {
      existing.units.push(contact);
    } else {
      groups.set(contact.partyId, {
        partyId: contact.partyId,
        partyFullName: contact.partyFullName,
        partyType: contact.partyType,
        partyPhone: contact.partyPhone,
        hasLinkedAccount: contact.hasLinkedAccount,
        units: [contact],
      });
    }
  }
  return Array.from(groups.values());
}

function bulkInviteSummaryMessage(result: { invited: number; alreadyLinked: number; failed: number }): string {
  return [
    result.invited > 0 ? `${result.invited} invitation(s) envoyée(s)` : null,
    result.alreadyLinked > 0 ? `${result.alreadyLinked} contact(s) déjà lié(s) (ignoré(s))` : null,
    result.failed > 0 ? `${result.failed} échec(s)` : null,
  ]
    .filter(Boolean)
    .join(' · ');
}

export function PropertyContactsTab() {
  const { property } = useOutletContext<{ property: Property }>();
  const [page, setPage] = useState(0);
  const [search, setSearch] = useState('');
  const [selectedPartyIds, setSelectedPartyIds] = useState<Set<string>>(new Set());
  const contacts = usePropertyContacts(property.id, page, search || undefined);
  const contactGroups = useMemo(() => groupByParty(contacts.data?.content ?? []), [contacts.data]);
  const invitableGroups = useMemo(() => contactGroups.filter((group) => !group.hasLinkedAccount), [contactGroups]);
  const bulkInvite = useBulkInviteParties();

  function changePage(newPage: number) {
    setPage(newPage);
    setSelectedPartyIds(new Set());
  }

  function toggleSelection(partyId: string) {
    setSelectedPartyIds((previous) => {
      const next = new Set(previous);
      if (next.has(partyId)) {
        next.delete(partyId);
      } else {
        next.add(partyId);
      }
      return next;
    });
  }

  function toggleSelectAll() {
    setSelectedPartyIds((previous) =>
      previous.size === invitableGroups.length ? new Set() : new Set(invitableGroups.map((group) => group.partyId)),
    );
  }

  function handleBulkInvite() {
    bulkInvite.mutate(Array.from(selectedPartyIds), {
      onSuccess: () => setSelectedPartyIds(new Set()),
    });
  }

  return (
    <Card className="flex flex-col gap-4">
      <Input
        label="Rechercher un contact (nom, téléphone)"
        value={search}
        onChange={(e) => {
          setSearch(e.target.value);
          setPage(0);
          setSelectedPartyIds(new Set());
        }}
      />
      {contacts.isLoading && <Loader label="Chargement des contacts…" />}
      {contacts.isError && <Alert message={getErrorMessage(contacts.error)} />}
      {contacts.data && contactGroups.length === 0 && (
        <EmptyState title={search ? 'Aucun contact trouvé' : 'Aucun contact pour le moment'}>
          {search
            ? 'Ajustez votre recherche.'
            : 'Les copropriétaires apparaîtront ici une fois rattachés à un lot.'}
        </EmptyState>
      )}
      {contacts.data && contactGroups.length > 0 && (
        <>
          {invitableGroups.length > 0 && (
            <div className="flex flex-wrap items-center justify-between gap-2">
              <label className="flex items-center gap-2 text-sm text-gray-600">
                <input
                  type="checkbox"
                  checked={selectedPartyIds.size > 0 && selectedPartyIds.size === invitableGroups.length}
                  onChange={toggleSelectAll}
                  className="h-4 w-4 rounded border-gray-300"
                />
                Tout sélectionner
              </label>
              <Button
                type="button"
                variant="secondary"
                disabled={selectedPartyIds.size === 0}
                isLoading={bulkInvite.isPending}
                onClick={handleBulkInvite}
              >
                Envoyer une invitation ({selectedPartyIds.size})
              </Button>
            </div>
          )}
          {bulkInvite.isSuccess && (
            <Alert
              variant={bulkInvite.data.failed > 0 ? 'warning' : 'success'}
              message={bulkInviteSummaryMessage(bulkInvite.data)}
            />
          )}
          {bulkInvite.isError && <Alert message={getErrorMessage(bulkInvite.error)} />}

          <ul className="flex flex-col divide-y divide-gray-200 rounded-lg border border-gray-200">
            {contactGroups.map((group) => {
              const secondaryLine = [
                group.partyPhone,
                group.units.map((unit) => `${unit.buildingName} — Lot ${unit.unitNumber}`).join(', '),
              ]
                .filter(Boolean)
                .join(' · ');

              return (
                <li key={group.partyId} className="flex items-center gap-3 px-3 py-2 hover:bg-gray-50">
                  <input
                    type="checkbox"
                    checked={selectedPartyIds.has(group.partyId)}
                    disabled={group.hasLinkedAccount}
                    onChange={() => toggleSelection(group.partyId)}
                    className="h-4 w-4 shrink-0 rounded border-gray-300 disabled:opacity-40"
                  />
                  <Link
                    to={`/parties/${property.id}/${group.partyId}`}
                    className="flex min-w-0 flex-1 items-center justify-between gap-2"
                  >
                    <div className="min-w-0">
                      <p className="truncate text-sm font-medium text-gray-900">
                        {group.partyFullName}{' '}
                        <span className="font-normal text-gray-500">({PARTY_TYPE_LABELS[group.partyType]})</span>
                      </p>
                      {secondaryLine && <p className="truncate text-sm text-gray-500">{secondaryLine}</p>}
                    </div>
                    {group.hasLinkedAccount && (
                      <span className="flex shrink-0 items-center gap-1 text-xs font-medium text-success-600">
                        <CheckCircleIcon className="h-4 w-4" />
                        <span>Compte lié</span>
                      </span>
                    )}
                  </Link>
                </li>
              );
            })}
          </ul>
          <Pagination pageNumber={contacts.data.pageNumber} totalPages={contacts.data.totalPages} onPageChange={changePage} />
        </>
      )}
    </Card>
  );
}
