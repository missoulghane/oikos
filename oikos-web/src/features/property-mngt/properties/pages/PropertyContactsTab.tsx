import { useMemo } from 'react';
import { useOutletContext } from 'react-router-dom';
import { Link } from 'react-router-dom';
import { usePropertyContacts } from '@/features/property-mngt/properties/hooks/usePropertyContacts';
import { PARTY_TYPE_LABELS } from '@/features/property-mngt/properties/constants/partyTypeLabels';
import { Loader } from '@/shared/components/Loader/Loader';
import { Alert } from '@/shared/components/Alert/Alert';
import { Badge } from '@/shared/components/Badge/Badge';
import { EmptyState } from '@/shared/components/EmptyState/EmptyState';
import { getErrorMessage } from '@/shared/utils/getErrorMessage';
import type { Property, PropertyContact } from '@/features/property-mngt/properties/types/property.types';

interface ContactGroup {
  partyId: string;
  partyFullName: string;
  partyType: PropertyContact['partyType'];
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
        hasLinkedAccount: contact.hasLinkedAccount,
        units: [contact],
      });
    }
  }
  return Array.from(groups.values());
}

export function PropertyContactsTab() {
  const { property } = useOutletContext<{ property: Property }>();
  const contacts = usePropertyContacts(property.id);
  const contactGroups = useMemo(() => groupByParty(contacts.data ?? []), [contacts.data]);

  if (contacts.isLoading) {
    return <Loader label="Chargement des contacts…" />;
  }

  if (contacts.isError) {
    return <Alert message={getErrorMessage(contacts.error)} />;
  }

  if (contactGroups.length === 0) {
    return (
      <EmptyState title="Aucun contact pour le moment">
        Les copropriétaires apparaîtront ici une fois rattachés à un lot.
      </EmptyState>
    );
  }

  return (
    <ul className="flex flex-col divide-y divide-gray-200 rounded-lg border border-gray-200">
      {contactGroups.map((group) => (
        <li key={group.partyId} className="flex flex-col gap-1 px-3 py-2">
          <p className="flex flex-wrap items-center gap-2 text-sm font-medium text-gray-900">
            <span>
              <Link to={`/properties/${property.id}/parties/${group.partyId}`} className="hover:underline">
                {group.partyFullName}
              </Link>{' '}
              ({PARTY_TYPE_LABELS[group.partyType]})
            </span>
            {group.hasLinkedAccount ? (
              <Badge color="success">Compte lié</Badge>
            ) : (
              <Badge color="light">Sans compte</Badge>
            )}
          </p>
          <ul className="flex flex-col gap-1">
            {group.units.map((unit) => (
              <li
                key={unit.id}
                className="flex flex-col gap-1 sm:flex-row sm:items-center sm:justify-between sm:gap-2"
              >
                <Link
                  to={`/properties/${property.id}/units/${unit.unitId}`}
                  className="text-sm text-gray-500 hover:underline"
                >
                  {unit.buildingName} — Lot {unit.unitNumber}
                </Link>
                <span className="w-fit rounded-full bg-gray-100 px-2 py-1 text-xs font-medium text-gray-600">
                  {unit.ownershipShare}%
                </span>
              </li>
            ))}
          </ul>
        </li>
      ))}
    </ul>
  );
}
