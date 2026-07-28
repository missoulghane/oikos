import { Link, useParams } from 'react-router-dom';
import { useParty } from '@/features/property-mngt/parties/hooks/useParty';
import { usePartyLots } from '@/features/property-mngt/parties/hooks/usePartyLots';
import { PARTY_TYPE_LABELS } from '@/features/property-mngt/properties/constants/partyTypeLabels';
import { Card } from '@/shared/components/Card/Card';
import { Loader } from '@/shared/components/Loader/Loader';
import { Alert } from '@/shared/components/Alert/Alert';
import { EmptyState } from '@/shared/components/EmptyState/EmptyState';
import { getErrorMessage } from '@/shared/utils/getErrorMessage';

export function PartyDetailPage() {
  const { id } = useParams<{ id: string }>();
  const partyId = id ?? '';
  const party = useParty(partyId);
  const lots = usePartyLots(partyId);

  if (party.isLoading) {
    return <Loader label="Chargement du contact…" />;
  }

  if (party.isError) {
    return <Alert message={getErrorMessage(party.error)} />;
  }

  if (!party.data) {
    return null;
  }

  return (
    <div className="flex flex-col gap-6">
      <div>
        <Link to="/parties" className="text-sm text-slate-500 hover:underline">
          ← Retour aux contacts
        </Link>
        <h1 className="text-lg font-semibold text-slate-900">{party.data.fullName}</h1>
        <p className="text-sm text-slate-500">
          {PARTY_TYPE_LABELS[party.data.partyType]} · {party.data.email}
          {party.data.phone && ` · ${party.data.phone}`}
        </p>
      </div>

      <Card className="flex flex-col gap-2">
        <h2 className="text-base font-semibold text-slate-900">Lots associés</h2>

        {lots.isLoading && <Loader label="Chargement des lots…" />}
        {lots.isError && <Alert message={getErrorMessage(lots.error)} />}
        {lots.data && lots.data.length === 0 && (
          <EmptyState title="Aucun lot associé">Ce contact n'est rattaché à aucun lot pour le moment.</EmptyState>
        )}
        {lots.data && lots.data.length > 0 && (
          <ul className="flex flex-col divide-y divide-slate-100">
            {lots.data.map((lot) => (
              <li key={lot.id} className="flex items-center justify-between py-2 text-sm">
                <Link to={`/units/${lot.unitId}`} className="text-slate-700 hover:underline">
                  {lot.propertyName} — {lot.buildingName} — Lot {lot.unitNumber}
                </Link>
                <span className="text-slate-500">{lot.ownershipShare}%</span>
              </li>
            ))}
          </ul>
        )}
      </Card>
    </div>
  );
}
