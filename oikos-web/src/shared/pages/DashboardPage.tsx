import { Link } from 'react-router-dom';
import type { ReactNode } from 'react';
import { ArrowDownIcon, ArrowRightIcon, ArrowUpIcon } from '@/shared/icons';
import { useCurrentUser, boardPropertyIds, canWriteAccounting } from '@/features/identity/me';
import { useEffectiveSpace } from '@/shared/hooks/useEffectiveSpace';
import { useMandateProperties } from '@/shared/hooks/useMandateProperties';
import { useProperty } from '@/features/property-mngt/properties/hooks/useProperty';
import { useProperties } from '@/features/property-mngt/properties/hooks/useProperties';
import { usePropertyUnitCount } from '@/features/property-mngt/properties/hooks/usePropertyUnitCount';
import { useLedgerAccounts } from '@/features/property-mngt/accounting/hooks/useLedgerAccounts';
import { TreasuryAccountCard } from '@/features/property-mngt/accounting/components/TreasuryAccountCard';
import { useInstallmentCollectionSummary } from '@/features/property-mngt/installments';
import type { Property } from '@/features/property-mngt/properties/types/property.types';
import { MyUnitsList } from '@/features/property-ownership/units/components/MyUnitsList';
import { ResumeOnboardingBanner } from '@/features/identity/onboarding/components/ResumeOnboardingBanner';
import { Card } from '@/shared/components/Card/Card';
import { CardLink } from '@/shared/components/Card/CardLink';
import { Loader } from '@/shared/components/Loader/Loader';
import { Alert } from '@/shared/components/Alert/Alert';
import { getErrorMessage } from '@/shared/utils/getErrorMessage';
import { getFirstName } from '@/shared/utils/getFirstName';
import { getGreeting } from '@/shared/utils/getGreeting';

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

/**
 * La copropriété elle-même : son nom, son adresse, ses lots. Une carte
 * cliquable plutôt qu'un titre suivi d'un bouton - c'est l'identité de ce que
 * le tableau de bord résume, et le geste attendu dessus est d'aller la
 * consulter.
 */
function PropertyIdentityCard({ property, propertyId }: { property: Property; propertyId: string }) {
  const unitCount = usePropertyUnitCount(propertyId);

  return (
    <CardLink to={`/property-mngt/properties/${propertyId}/property`} className="flex flex-col gap-1">
      <h2 className="text-base font-semibold text-gray-900 dark:text-white/90">{property.name}</h2>
      <p className="text-sm text-gray-500 dark:text-gray-400">{property.address}</p>
      {/* Le compte se charge après le reste : la carte s'affiche sans l'attendre plutôt
          que de retenir le nom et l'adresse, déjà connus. */}
      <p className="text-sm text-gray-400 dark:text-gray-500">
        {unitCount.data === undefined ? '—' : `${unitCount.data} lot${unitCount.data > 1 ? 's' : ''}`}
      </p>
    </CardLink>
  );
}

/**
 * Les soldes de trésorerie, comme sur la vue d'ensemble de la comptabilité et
 * par le même composant : un solde qui se lirait différemment de deux écrans
 * serait un solde qu'on vérifie ailleurs.
 */
function TreasuryBalances({ propertyId }: { propertyId: string }) {
  const ledgerAccounts = useLedgerAccounts(propertyId);
  const treasuryAccounts = (ledgerAccounts.data ?? []).filter(
    (account) => account.role === 'CASH' || account.role === 'BANK',
  );

  if (ledgerAccounts.isError) {
    return <Alert message={getErrorMessage(ledgerAccounts.error)} />;
  }

  if (treasuryAccounts.length === 0) {
    return null;
  }

  return (
    <div className="grid grid-cols-1 gap-4 sm:grid-cols-2 lg:grid-cols-3">
      {treasuryAccounts.map((account) => (
        <TreasuryAccountCard key={account.id} account={account} propertyId={propertyId} />
      ))}
    </div>
  );
}

/**
 * Ce qu'il reste à encaisser : les échéances non soldées et déjà échues. Le
 * lien ouvre exactement l'ensemble compté - filtre « Non soldée », et « à
 * venir » resté à false, qui est le défaut de cet écran.
 */
function CollectionCard({ propertyId }: { propertyId: string }) {
  const summary = useInstallmentCollectionSummary(propertyId);

  return (
    <CardLink
      to={`/property-mngt/properties/${propertyId}/installments?status=NOT_SETTLED`}
      className="flex flex-col gap-1"
    >
      <p className="text-sm text-gray-500 dark:text-gray-400">À collecter</p>
      {summary.isError ? (
        <Alert message={getErrorMessage(summary.error)} />
      ) : (
        <>
          <p className="text-2xl font-semibold text-gray-900 dark:text-white/90">
            {summary.data ? `${summary.data.amount.toLocaleString('fr-FR')} MAD` : '—'}
          </p>
          <p className="text-sm text-gray-400 dark:text-gray-500">
            {summary.data
              ? `${summary.data.count} échéance${summary.data.count > 1 ? 's' : ''} non soldée${
                  summary.data.count > 1 ? 's' : ''
                } et échue${summary.data.count > 1 ? 's' : ''}`
              : 'Chargement…'}
          </p>
        </>
      )}
    </CardLink>
  );
}

/**
 * Les deux gestes que le syndic vient faire le plus souvent : encaisser et
 * payer. Ils étaient sous les soldes, en boutons discrets, c'est-à-dire après
 * les chiffres qu'ils servent justement à alimenter - on les cherchait.
 *
 * <p>Deux cartes plutôt que deux boutons : à cette place, en tête d'écran, la
 * surface tient lieu d'emphase, et la place gagnée porte ce que le bouton seul
 * ne disait pas - ce qu'on va y saisir. La flèche annonce qu'on change de page,
 * là où un bouton laisse croire à une action immédiate.
 */
function AccountingEntryActions({ propertyId }: { propertyId: string }) {
  return (
    <div className="grid grid-cols-1 gap-4 sm:grid-cols-2">
      <EntryActionCard
        to={`/property-mngt/properties/${propertyId}/accounting/receipts/new`}
        icon={<ArrowDownIcon className="size-5" />}
        title="Saisir une recette"
        subtitle="Appel de fonds, règlement…"
      />
      <EntryActionCard
        to={`/property-mngt/properties/${propertyId}/accounting/supplier-payments/new`}
        icon={<ArrowUpIcon className="size-5" />}
        title="Saisir une dépense"
        subtitle="Facture, fournisseur, paiement…"
      />
    </div>
  );
}

interface EntryActionCardProps {
  to: string;
  icon: ReactNode;
  title: string;
  subtitle: string;
}

/**
 * Le survol soulève la carte d'un pixel : assez pour qu'elle se signale comme
 * cliquable, trop peu pour bouger la page. La bordure fonce en même temps, pour
 * ceux qui ne perçoivent pas ce déplacement.
 */
function EntryActionCard({ to, icon, title, subtitle }: EntryActionCardProps) {
  return (
    <Link
      to={to}
      className="flex items-center justify-between gap-4 rounded-2xl border border-gray-200 bg-white p-5 shadow-theme-xs transition duration-200 hover:-translate-y-px hover:border-gray-300 hover:shadow-theme-md dark:border-gray-800 dark:bg-white/[0.03] dark:hover:border-gray-700"
    >
      <span className="flex min-w-0 items-center gap-4">
        <span className="flex size-11 shrink-0 items-center justify-center rounded-xl bg-gray-100 text-gray-700 dark:bg-white/[0.06] dark:text-gray-300">
          {icon}
        </span>
        <span className="min-w-0">
          <span className="block text-base font-semibold text-gray-900 dark:text-white/90">{title}</span>
          <span className="block truncate text-sm text-gray-500 dark:text-gray-400">{subtitle}</span>
        </span>
      </span>
      <ArrowRightIcon className="size-5 shrink-0 text-gray-400 dark:text-gray-500" />
    </Link>
  );
}

function BoardDashboard({ propertyId, mandateIds }: { propertyId: string; mandateIds: string[] }) {
  const property = useProperty(propertyId);
  const currentUser = useCurrentUser();
  // Les deux saisies ne s'affichent que pour qui peut écrire en comptabilité - un bouton
  // qui mène à un formulaire refusé n'est pas un raccourci.
  const canWrite = currentUser.data ? canWriteAccounting(currentUser.data, propertyId) : false;

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

      {canWrite && <AccountingEntryActions propertyId={propertyId} />}

      <div className="grid grid-cols-1 gap-4 sm:grid-cols-2">
        <PropertyIdentityCard property={property.data} propertyId={propertyId} />
        <CollectionCard propertyId={propertyId} />
      </div>

      <TreasuryBalances propertyId={propertyId} />
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

/**
 * The owner's dashboard IS their lots - "Mes lots" was folded in here rather
 * than living as a separate screen, so the landing page opens on something
 * actionable instead of a summary of numbers found one click away anyway.
 *
 * <p>« Mes lots » a quitté le titre de la page, qui nomme désormais l'espace,
 * et redevient ce qu'il est : l'intitulé de la liste qui suit.
 */
function OwnerDashboard() {
  return (
    <>
      <h2 className="text-base font-semibold text-gray-900 dark:text-white/90">Mes lots</h2>
      <MyUnitsList />
    </>
  );
}

/**
 * Le nom de l'espace où l'on vient d'arriver. « Conseil syndical » et non
 * « bureau », comme partout ailleurs dans le produit depuis le renommage ; et
 * « gestion » pour le cabinet, qui administre des copropriétés sans siéger dans
 * aucune.
 */
const SPACE_WELCOME: Record<'owner' | 'board' | 'manager', string> = {
  owner: 'Bienvenue dans votre espace copropriétaire',
  board: 'Bienvenue dans votre espace conseil syndical',
  manager: 'Bienvenue dans votre espace gestion',
};

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
      <OwnerDashboard />
    );

  const firstName = currentUser.data ? getFirstName(currentUser.data.fullName) : '';

  return (
    <div className="flex flex-col gap-6">
      {/* La salutation dit à qui on parle, la ligne dessous depuis où : un même
          compte peut être copropriétaire ici et au conseil syndical là, et
          l'écran change entièrement d'un espace à l'autre. Le titre ne portait
          que « Mes lots » côté copropriétaire, ce qui nommait le contenu sans
          jamais nommer l'espace. */}
      <div className="flex flex-col gap-1">
        <h1 className="text-lg font-semibold text-gray-900 dark:text-white/90">
          {getGreeting()} {firstName}
        </h1>
        <p className="text-sm text-gray-500 dark:text-gray-400">{SPACE_WELCOME[effectiveSpace.kind]}</p>
      </div>
      {content}
    </div>
  );
}
