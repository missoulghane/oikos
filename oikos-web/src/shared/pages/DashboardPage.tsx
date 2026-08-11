import { Link } from 'react-router-dom';
import { useCurrentUser, boardPropertyId, isManagerTier } from '@/features/identity/me';
import { useProperty } from '@/features/property-mngt/properties/hooks/useProperty';
import { useProperties } from '@/features/property-mngt/properties/hooks/useProperties';
import { useMyUnits } from '@/features/property-ownership/units';
import { useMyInstallments } from '@/features/property-ownership/installments/hooks/useMyInstallments';
import { ResumeOnboardingBanner } from '@/features/identity/onboarding/components/ResumeOnboardingBanner';
import { Card } from '@/shared/components/Card/Card';
import { Loader } from '@/shared/components/Loader/Loader';
import { Alert } from '@/shared/components/Alert/Alert';
import { getErrorMessage } from '@/shared/utils/getErrorMessage';

function BoardDashboard({ propertyId }: { propertyId: string }) {
  const property = useProperty(propertyId);

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
      <ResumeOnboardingBanner />
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

function OwnerDashboard() {
  const units = useMyUnits();
  const installments = useMyInstallments();

  if (units.isLoading) {
    return <Loader label="Chargement de vos lots…" />;
  }

  if (units.isError) {
    return <Alert message={getErrorMessage(units.error)} />;
  }

  const count = units.data?.length ?? 0;
  const unpaidCount = installments.data?.filter((installment) => installment.status !== 'SETTLED').length ?? 0;

  return (
    <Card className="flex flex-col gap-4">
      <div>
        <h2 className="text-base font-semibold text-gray-900 dark:text-white/90">
          Vous possédez {count} lot{count > 1 ? 's' : ''}
        </h2>
        {unpaidCount > 0 && (
          <p className="text-sm text-gray-500 dark:text-gray-400">
            {unpaidCount} échéance{unpaidCount > 1 ? 's' : ''} en attente de règlement
          </p>
        )}
      </div>
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
  );
}

export function DashboardPage() {
  const currentUser = useCurrentUser();

  if (currentUser.isLoading) {
    return <Loader />;
  }

  const boardId = currentUser.data ? boardPropertyId(currentUser.data) : null;
  const isManager = currentUser.data ? isManagerTier(currentUser.data) : false;

  return (
    <div className="flex flex-col gap-6">
      <h1 className="text-lg font-semibold text-gray-900 dark:text-white/90">Tableau de bord</h1>
      {boardId ? (
        <BoardDashboard propertyId={boardId} />
      ) : isManager ? (
        <ManagerDashboard />
      ) : (
        <OwnerDashboard />
      )}
    </div>
  );
}
