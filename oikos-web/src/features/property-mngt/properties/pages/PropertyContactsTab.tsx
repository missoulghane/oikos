import { useMemo, useState } from 'react';
import { useOutletContext } from 'react-router-dom';
import { Link } from 'react-router-dom';
import { usePropertyContacts } from '@/features/property-mngt/properties/hooks/usePropertyContacts';
import { boardSpaceQuerySuffix } from '@/shared/hooks/useEffectiveSpace';
import { PARTY_TYPE_LABELS } from '@/features/property-mngt/properties/constants/partyTypeLabels';
import { Card } from '@/shared/components/Card/Card';
import { Loader } from '@/shared/components/Loader/Loader';
import { Alert } from '@/shared/components/Alert/Alert';
import { Select } from '@/shared/components/Select/Select';
import { FilterPanel } from '@/shared/components/FilterPanel/FilterPanel';
import { SortableColumnHeader } from '@/shared/components/SortableColumnHeader/SortableColumnHeader';
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
  }
  const contactGroups = useMemo(() => groupByParty(contacts.data?.content ?? []), [contacts.data]);

  return (
    <Card className="flex flex-col gap-4">
      <FilterPanel
        activeCount={countActiveFilters({ accountFilter, search }, { accountFilter: '', search: '' })}
        onClear={() => {
          setSearch('');
          setAccountFilter('');
          setPage(0);
        }}
        search={{
          value: search,
          onChange: (next) => {
            setSearch(next);
            setPage(0);
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
          <div className="overflow-x-auto rounded-lg border border-gray-200 dark:border-gray-800">
            <table className="min-w-full divide-y divide-gray-200 dark:divide-gray-800 text-sm">
              <thead>
                <tr className="text-left text-gray-500 dark:text-gray-400">
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
                      {/* Le suffixe d'espace suit le lien : la fiche contact vit hors
                          de /property-mngt (voir boardSpaceQuerySuffix), et sans lui
                          le syndic basculait dans son espace copropriétaire. */}
                      <Link
                        to={`/parties/${property.id}/${group.partyId}${boardSpaceQuerySuffix(property.id)}`}
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
                      {group.units.map((unit) => `${unit.buildingName} — ${unit.unitNumber}`).join(', ')}
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
          <Pagination pageNumber={contacts.data.pageNumber} totalPages={contacts.data.totalPages} onPageChange={setPage} />
        </>
      )}
    </Card>
  );
}
