import { useOutletContext } from 'react-router-dom';
import { Link } from 'react-router-dom';
import { usePropertyContacts } from '@/features/property-mngt/properties/hooks/usePropertyContacts';
import { PARTY_TYPE_LABELS } from '@/features/property-mngt/properties/constants/partyTypeLabels';
import { Loader } from '@/shared/components/Loader/Loader';
import { Alert } from '@/shared/components/Alert/Alert';
import { EmptyState } from '@/shared/components/EmptyState/EmptyState';
import { getErrorMessage } from '@/shared/utils/getErrorMessage';
import type { Property } from '@/features/property-mngt/properties/types/property.types';

export function PropertyContactsTab() {
  const { property } = useOutletContext<{ property: Property }>();
  const contacts = usePropertyContacts(property.id);

  if (contacts.isLoading) {
    return <Loader label="Chargement des contacts…" />;
  }

  if (contacts.isError) {
    return <Alert message={getErrorMessage(contacts.error)} />;
  }

  if (!contacts.data || contacts.data.length === 0) {
    return (
      <EmptyState title="Aucun contact pour le moment">
        Les copropriétaires apparaîtront ici une fois rattachés à un lot.
      </EmptyState>
    );
  }

  return (
    <ul className="flex flex-col divide-y divide-slate-200 rounded-md border border-slate-200">
      {contacts.data.map((contact) => (
        <li key={contact.id} className="flex flex-col gap-1 px-3 py-2 sm:flex-row sm:items-center sm:justify-between">
          <div>
            <p className="text-sm font-medium text-slate-900">
              <Link to={`/parties/${contact.partyId}`} className="hover:underline">
                {contact.partyFullName}
              </Link>{' '}
              ({PARTY_TYPE_LABELS[contact.partyType]})
            </p>
            <p className="text-sm text-slate-500">
              <Link to={`/units/${contact.unitId}`} className="hover:underline">
                {contact.buildingName} — Lot {contact.unitNumber}
              </Link>
            </p>
          </div>
          <span className="w-fit rounded-full bg-slate-100 px-2 py-1 text-xs font-medium text-slate-600">
            {contact.ownershipShare}%
          </span>
        </li>
      ))}
    </ul>
  );
}
