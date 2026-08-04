import { useMemo, useState } from 'react';
import { useOutletContext } from 'react-router-dom';
import { Link } from 'react-router-dom';
import { usePropertyContacts } from '@/features/property-mngt/properties/hooks/usePropertyContacts';
import { PARTY_TYPE_LABELS } from '@/features/property-mngt/properties/constants/partyTypeLabels';
import { Card } from '@/shared/components/Card/Card';
import { Loader } from '@/shared/components/Loader/Loader';
import { Alert } from '@/shared/components/Alert/Alert';
import { Input } from '@/shared/components/Input/Input';
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

export function PropertyContactsTab() {
  const { property } = useOutletContext<{ property: Property }>();
  const [page, setPage] = useState(0);
  const [search, setSearch] = useState('');
  const contacts = usePropertyContacts(property.id, page, search || undefined);
  const contactGroups = useMemo(() => groupByParty(contacts.data?.content ?? []), [contacts.data]);

  return (
    <Card className="flex flex-col gap-4">
      <Input
        label="Rechercher un contact (nom, téléphone)"
        value={search}
        onChange={(e) => {
          setSearch(e.target.value);
          setPage(0);
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
          <ul className="flex flex-col divide-y divide-gray-200 rounded-lg border border-gray-200">
            {contactGroups.map((group) => {
              const secondaryLine = [
                group.partyPhone,
                group.units.map((unit) => `${unit.buildingName} — Lot ${unit.unitNumber}`).join(', '),
              ]
                .filter(Boolean)
                .join(' · ');

              return (
                <li key={group.partyId}>
                  <Link
                    to={`/parties/${property.id}/${group.partyId}`}
                    className="flex items-center justify-between gap-2 px-3 py-2 hover:bg-gray-50"
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
          <Pagination pageNumber={contacts.data.pageNumber} totalPages={contacts.data.totalPages} onPageChange={setPage} />
        </>
      )}
    </Card>
  );
}
