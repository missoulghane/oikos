import { useMemo, useState } from 'react';
import { useOutletContext } from 'react-router-dom';
import { Link } from 'react-router-dom';
import { usePropertyContacts } from '@/features/property-mngt/properties/hooks/usePropertyContacts';
import { PARTY_TYPE_LABELS } from '@/features/property-mngt/properties/constants/partyTypeLabels';
import { useBulkInviteParties } from '@/features/property-mngt/parties/hooks/useBulkInviteParties';
import { Card } from '@/shared/components/Card/Card';
import { Loader } from '@/shared/components/Loader/Loader';
import { Alert } from '@/shared/components/Alert/Alert';
import { Select } from '@/shared/components/Select/Select';
import { FilterPanel } from '@/shared/components/FilterPanel/FilterPanel';
import { SortableColumnHeader } from '@/shared/components/SortableColumnHeader/SortableColumnHeader';
import { Button } from '@/shared/components/Button/Button';
import { Pagination } from '@/shared/components/Pagination/Pagination';
import { EmptyState } from '@/shared/components/EmptyState/EmptyState';
import { getErrorMessage } from '@/shared/utils/getErrorMessage';
import { countActiveFilters } from '@/shared/utils/countActiveFilters';
import { nextSortDirection, type SortDirection } from '@/shared/utils/sorting';
import { CheckCircleIcon } from '@/shared/icons';
import type {
  ContactSortField,
  Property,
  PropertyContact,
} from '@/features/property-mngt/properties/types/property.types';

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

// '' is the "no filter" choice; 'LINKED'/'NOT_LINKED' map to the
// hasLinkedAccount boolean the API expects (a <select> value is always a
// string, so the tri-state cannot be a boolean here).
type AccountFilter = '' | 'LINKED' | 'NOT_LINKED';

function hasLinkedAccountFilter(accountFilter: AccountFilter): boolean | undefined {
  if (accountFilter === '') return undefined;
  return accountFilter === 'LINKED';
}

export function PropertyContactsTab() {
  const { property } = useOutletContext<{ property: Property }>();
  const [page, setPage] = useState(0);
  const [search, setSearch] = useState('');
  const [accountFilter, setAccountFilter] = useState<AccountFilter>('');
  const [selectedPartyIds, setSelectedPartyIds] = useState<Set<string>>(new Set());
  const isFiltered = search !== '' || accountFilter !== '';
  // Name ascending: the order the list was already returned in before it became
  // sortable, so the default view is unchanged.
  const [sortBy, setSortBy] = useState<ContactSortField>('FULL_NAME');
  const [sortDirection, setSortDirection] = useState<SortDirection>('ASC');
  const contacts = usePropertyContacts(property.id, page, {
    search: search || undefined,
    hasLinkedAccount: hasLinkedAccountFilter(accountFilter),
    sortBy,
    sortDirection,
  });

  function handleSort(field: ContactSortField) {
    setSortDirection(nextSortDirection(field, sortBy, sortDirection));
    setSortBy(field);
    setPage(0);
    setSelectedPartyIds(new Set());
  }
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
      <FilterPanel
        activeCount={countActiveFilters({ accountFilter, search }, { accountFilter: '', search: '' })}
        onClear={() => {
          setSearch('');
          setAccountFilter('');
          setPage(0);
          setSelectedPartyIds(new Set());
        }}
        search={{
          value: search,
          onChange: (next) => {
            setSearch(next);
            setPage(0);
            setSelectedPartyIds(new Set());
          },
          placeholder: 'Rechercher un contact, un téléphone…',
        }}
      >
        <div className="grid grid-cols-1 gap-3 sm:grid-cols-2 lg:grid-cols-4">
          <Select
            label="Compte utilisateur"
            // Input/Select derive the label's htmlFor from id ?? name - without
            // one the label stays detached from the field.
            name="account-filter"
            value={accountFilter}
            onChange={(e) => {
              setAccountFilter(e.target.value as AccountFilter);
              setPage(0);
              setSelectedPartyIds(new Set());
            }}
          >
            <option value="">Tous les contacts</option>
            <option value="LINKED">Compte actif</option>
            <option value="NOT_LINKED">Sans compte</option>
          </Select>
        </div>
      </FilterPanel>
      {contacts.isLoading && <Loader label="Chargement des contacts…" />}
      {contacts.isError && <Alert message={getErrorMessage(contacts.error)} />}
      {contacts.data && contactGroups.length === 0 && (
        <EmptyState title={isFiltered ? 'Aucun contact trouvé' : 'Aucun contact pour le moment'}>
          {isFiltered
            ? 'Ajustez votre recherche ou vos filtres.'
            : 'Les copropriétaires apparaîtront ici une fois rattachés à un lot.'}
        </EmptyState>
      )}
      {contacts.data && contactGroups.length > 0 && (
        <>
          {invitableGroups.length > 0 && (
            <div className="flex flex-wrap items-center justify-between gap-2">
              <label className="flex items-center gap-2 text-sm text-gray-600 dark:text-gray-400">
                <input
                  type="checkbox"
                  checked={selectedPartyIds.size > 0 && selectedPartyIds.size === invitableGroups.length}
                  onChange={toggleSelectAll}
                  className="h-4 w-4 rounded border-gray-300 dark:border-gray-700"
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

          <div className="overflow-x-auto rounded-lg border border-gray-200 dark:border-gray-800">
            <table className="min-w-full divide-y divide-gray-200 dark:divide-gray-800 text-sm">
              <thead>
                <tr className="text-left text-gray-500 dark:text-gray-400">
                  {/* The checkbox column has no header label: "select all" already
                      sits above the table, and duplicating it here would give two
                      controls for one action. */}
                  <th className="px-3 py-2">
                    <span className="sr-only">Sélection</span>
                  </th>
                  <SortableColumnHeader
                    field="FULL_NAME"
                    activeField={sortBy}
                    direction={sortDirection}
                    onSort={handleSort}
                    className="px-3"
                  >
                    Contact
                  </SortableColumnHeader>
                  <th className="px-3 py-2 font-medium">Téléphone</th>
                  <th className="px-3 py-2 font-medium">Lots</th>
                  <SortableColumnHeader
                    field="ACCOUNT_STATUS"
                    activeField={sortBy}
                    direction={sortDirection}
                    onSort={handleSort}
                    className="px-3"
                  >
                    Compte
                  </SortableColumnHeader>
                </tr>
              </thead>
              <tbody className="divide-y divide-gray-100 dark:divide-gray-800">
                {contactGroups.map((group) => (
                  <tr key={group.partyId} className="hover:bg-gray-50 dark:hover:bg-white/[0.03]">
                    <td className="px-3 py-2">
                      <input
                        type="checkbox"
                        aria-label={`Sélectionner ${group.partyFullName}`}
                        checked={selectedPartyIds.has(group.partyId)}
                        disabled={group.hasLinkedAccount}
                        onChange={() => toggleSelection(group.partyId)}
                        className="h-4 w-4 shrink-0 rounded border-gray-300 disabled:opacity-40 dark:border-gray-700"
                      />
                    </td>
                    <td className="px-3 py-2">
                      <Link
                        to={`/parties/${property.id}/${group.partyId}`}
                        className="font-medium text-gray-900 hover:underline dark:text-white/90"
                      >
                        {group.partyFullName}
                      </Link>{' '}
                      <span className="text-gray-500 dark:text-gray-400">
                        ({PARTY_TYPE_LABELS[group.partyType]})
                      </span>
                    </td>
                    <td className="px-3 py-2 text-gray-500 dark:text-gray-400">{group.partyPhone ?? '—'}</td>
                    <td className="px-3 py-2 text-gray-500 dark:text-gray-400">
                      {group.units.map((unit) => `${unit.buildingName} — Lot ${unit.unitNumber}`).join(', ')}
                    </td>
                    <td className="px-3 py-2">
                      {group.hasLinkedAccount ? (
                        <span className="flex items-center gap-1 text-xs font-medium text-success-600 dark:text-success-500">
                          <CheckCircleIcon className="h-4 w-4" />
                          <span>Compte actif</span>
                        </span>
                      ) : (
                        <span className="text-xs text-gray-400 dark:text-gray-500">Sans compte</span>
                      )}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
          <Pagination pageNumber={contacts.data.pageNumber} totalPages={contacts.data.totalPages} onPageChange={changePage} />
        </>
      )}
    </Card>
  );
}
