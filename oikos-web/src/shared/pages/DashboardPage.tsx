import { Link } from 'react-router-dom';
import { useCurrentUser, boardPropertyIds, hasCopro } from '@/features/identity/me';
import { useEffectiveSpace } from '@/shared/hooks/useEffectiveSpace';
import { useMandateProperties } from '@/shared/hooks/useMandateProperties';
import { useProperty } from '@/features/property-mngt/properties/hooks/useProperty';
import { useProperties } from '@/features/property-mngt/properties/hooks/useProperties';
import { useMyUnits } from '@/features/property-ownership/units';
import { useMyInstallments } from '@/features/property-ownership/installments/hooks/useMyInstallments';
import { ResumeOnboardingBanner } from '@/features/identity/onboarding/components/ResumeOnboardingBanner';
import { SpaceLinkCard } from '@/shared/components/SpaceLinkCard/SpaceLinkCard';
import { Card } from '@/shared/components/Card/Card';
import { Loader } from '@/shared/components/Loader/Loader';
import { Alert } from '@/shared/components/Alert/Alert';
import { getErrorMessage } from '@/shared/utils/getErrorMessage';
import { getFirstName } from '@/shared/utils/getFirstName';
import { getGreeting } from '@/shared/utils/getGreeting';

function money(amount: number): string {
  return `${amount.toLocaleString('fr-FR')} MAD`;
}

/**
 * Un mandat s'assume, il ne s'impose pas : quand plusieurs mandats existent,
 * chacun apparaît ici comme un raccourci - jamais comme une bascule
 * automatique de l'écran d'accueil.
 */
function MandatesStrip({ mandateIds, currentPropertyId }: { mandateIds: string[]; currentPropertyId?: string }) {
  const mandateProperties = useMandateProperties(mandateIds);

  if (mandateIds.length === 0) {
    return null;
  }

  return (
    <Card className="flex flex-col gap-3">
      <h3 className="text-sm font-semibold text-gray-900 dark:text-white/90">
        Vos mandats · {mandateIds.length}
      </h3>
      <div className="flex flex-wrap gap-2">
        {mandateIds.map((propertyId) => {
          const property = mandateProperties.byId.get(propertyId);
          const isCurrent = propertyId === currentPropertyId;
          return (
            <Link
              key={propertyId}
              to={`/dashboard?space=board&propertyId=${propertyId}`}
              aria-current={isCurrent}
              className={`rounded-lg px-3 py-2 text-sm font-medium ${
                isCurrent
                  ? 'bg-brand-500 text-white'
                  : 'bg-gray-100 text-gray-700 hover:bg-gray-200 dark:bg-white/[0.05] dark:text-gray-300 dark:hover:bg-white/[0.08]'
              }`}
            >
              {property?.name ?? 'Copropriété'}
            </Link>
          );
        })}
      </div>
    </Card>
  );
}

function BoardDashboard({ propertyId, mandateIds }: { propertyId: string; mandateIds: string[] }) {
  const property = useProperty(propertyId);
  const currentUser = useCurrentUser();
  const ownsHere = currentUser.data ? hasCopro(currentUser.data) : false;

  if (property.isLoading) {
    return <Loader label="Chargement de votre copropriété…" />;
  }

  if (property.isError) {
    return <Alert message={getErrorMessage(property.error)} />;
  }

  if (!property.data) {
    return null;
  }

  return (
    <>
      <ResumeOnboardingBanner propertyId={propertyId} />
      {mandateIds.length > 1 && <MandatesStrip mandateIds={mandateIds} currentPropertyId={propertyId} />}
      <Card className="flex flex-col gap-4">
        <div>
          <h2 className="text-base font-semibold text-gray-900 dark:text-white/90">{property.data.name}</h2>
          <p className="text-sm text-gray-500 dark:text-gray-400">{property.data.address}</p>
        </div>
        <div className="flex flex-wrap gap-3">
          <Link
            to={`/property-mngt/properties/${propertyId}/property`}
            className="rounded-lg bg-brand-500 px-4 py-2 text-sm font-medium text-white shadow-theme-xs hover:bg-brand-600"
          >
            Ma copropriété
          </Link>
          <Link
            to={`/property-mngt/properties/${propertyId}/installments`}
            className="rounded-lg px-4 py-2 text-sm font-medium text-gray-700 dark:text-gray-300 ring-1 ring-inset ring-gray-300 dark:ring-gray-700 hover:bg-gray-50 dark:hover:bg-white/[0.03]"
          >
            Gestion des échéances
          </Link>
        </div>
      </Card>
      {ownsHere && (
        <SpaceLinkCard
          to="/dashboard?space=owner"
          tone="owner"
          title="Revenir à l'espace copropriétaire"
          subtitle="Vos charges se règlent là, pas dans l'espace bureau"
        />
      )}
    </>
  );
}

function ManagerDashboard() {
  const properties = useProperties(0);

  if (properties.isLoading) {
    return <Loader label="Chargement de vos copropriétés…" />;
  }

  if (properties.isError) {
    return <Alert message={getErrorMessage(properties.error)} />;
  }

  const count = properties.data?.totalElements ?? 0;

  return (
    <Card className="flex flex-col gap-4">
      <div>
        <h2 className="text-base font-semibold text-gray-900 dark:text-white/90">
          Vous gérez {count} copropriété{count > 1 ? 's' : ''}
        </h2>
      </div>
      <Link
        to="/property-mngt/properties"
        className="w-fit rounded-lg bg-brand-500 px-4 py-2 text-sm font-medium text-white shadow-theme-xs hover:bg-brand-600"
      >
        Voir mes copropriétés
      </Link>
    </Card>
  );
}

function OwnerDashboard({ mandateIds }: { mandateIds: string[] }) {
  const units = useMyUnits();
  const installments = useMyInstallments();

  if (units.isLoading) {
    return <Loader label="Chargement de vos lots…" />;
  }

  if (units.isError) {
    return <Alert message={getErrorMessage(units.error)} />;
  }

  const unitCount = units.data?.length ?? 0;
  const propertyIds = new Set((units.data ?? []).map((unit) => unit.propertyId));
  const isMultiProperty = propertyIds.size > 1;

  const unpaid = (installments.data ?? []).filter((installment) => installment.status !== 'SETTLED');
  const totalDue = unpaid.reduce((sum, installment) => sum + installment.outstandingAmount, 0);

  const unitByUnitId = new Map((units.data ?? []).map((unit) => [unit.unitId, unit]));
  const dueByProperty = new Map<string, { propertyName: string; total: number }>();
  for (const installment of unpaid) {
    const unit = unitByUnitId.get(installment.unitId);
    if (!unit) continue;
    const entry = dueByProperty.get(unit.propertyId) ?? { propertyName: unit.propertyName, total: 0 };
    entry.total += installment.outstandingAmount;
    dueByProperty.set(unit.propertyId, entry);
  }

  return (
    <>
      <Card className="flex flex-col gap-4">
        <div>
          <h2 className="text-base font-semibold text-gray-900 dark:text-white/90">
            Solde à régler{isMultiProperty ? ' · toutes résidences' : ''}
          </h2>
          <p className="mt-1 text-2xl font-semibold text-gray-900 dark:text-white/90">{money(totalDue)}</p>
          <p className="text-sm text-gray-500 dark:text-gray-400">
            {unitCount} lot{unitCount > 1 ? 's' : ''}
            {isMultiProperty ? ` · ${propertyIds.size} résidences` : ''}
            {unpaid.length > 0
              ? ` · ${unpaid.length} échéance${unpaid.length > 1 ? 's' : ''} en attente`
              : ' · à jour'}
          </p>
        </div>
        {isMultiProperty && (
          <div className="flex flex-wrap gap-3">
            {[...dueByProperty.entries()].map(([propertyId, entry]) => (
              <div
                key={propertyId}
                className="rounded-lg bg-gray-50 px-3 py-2 text-sm dark:bg-white/[0.03]"
              >
                <span className="block text-gray-500 dark:text-gray-400">{entry.propertyName}</span>
                <span className="font-semibold text-gray-900 dark:text-white/90">{money(entry.total)}</span>
              </div>
            ))}
          </div>
        )}
        <div className="flex flex-wrap gap-3">
          <Link
            to="/property-ownership/units"
            className="rounded-lg bg-brand-500 px-4 py-2 text-sm font-medium text-white shadow-theme-xs hover:bg-brand-600"
          >
            Mes lots
          </Link>
          <Link
            to="/property-ownership/installments"
            className="rounded-lg px-4 py-2 text-sm font-medium text-gray-700 dark:text-gray-300 ring-1 ring-inset ring-gray-300 dark:ring-gray-700 hover:bg-gray-50 dark:hover:bg-white/[0.03]"
          >
            Mes échéances
          </Link>
        </div>
      </Card>
      {mandateIds.length > 0 && (
        <MandateCard mandateIds={mandateIds} />
      )}
    </>
  );
}

/** The card that signals a board mandate from the owner dashboard - a mention, never an automatic switch. */
function MandateCard({ mandateIds }: { mandateIds: string[] }) {
  const mandateProperties = useMandateProperties(mandateIds);
  const first = mandateProperties.byId.get(mandateIds[0]);

  return (
    <SpaceLinkCard
      to={`/dashboard?space=board&propertyId=${mandateIds[0]}`}
      tone="board"
      title={mandateIds.length > 1 ? `${mandateIds.length} mandats au bureau` : `Mandat au bureau — ${first?.name ?? ''}`}
      subtitle="Accéder à l'espace bureau de syndic"
    />
  );
}

export function DashboardPage() {
  const currentUser = useCurrentUser();
  const effectiveSpace = useEffectiveSpace();

  if (currentUser.isLoading || effectiveSpace.kind === 'unresolved') {
    return <Loader />;
  }

  const mandateIds = currentUser.data ? boardPropertyIds(currentUser.data) : [];

  // Le contenu suit exactement l'espace résolu par useEffectiveSpace (même
  // source que la sidebar et le sélecteur d'espace) : copropriétaire,
  // bureau d'un mandat précis, ou gérant professionnel.
  const content =
    effectiveSpace.kind === 'board' ? (
      <BoardDashboard propertyId={effectiveSpace.propertyId} mandateIds={mandateIds} />
    ) : effectiveSpace.kind === 'manager' ? (
      <ManagerDashboard />
    ) : (
      <OwnerDashboard mandateIds={mandateIds} />
    );

  const firstName = currentUser.data ? getFirstName(currentUser.data.fullName) : '';

  return (
    <div className="flex flex-col gap-6">
      <h1 className="text-lg font-semibold text-gray-900 dark:text-white/90">
        {getGreeting()} {firstName}
      </h1>
      {content}
    </div>
  );
}
