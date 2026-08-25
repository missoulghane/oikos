import { useState } from 'react';
import { Link, useOutletContext } from 'react-router-dom';
import { useBuildings } from '@/features/property-mngt/properties/hooks/useBuildings';
import { BuildingSection } from '@/features/property-mngt/properties/components/BuildingSection';
import { Loader } from '@/shared/components/Loader/Loader';
import { Alert } from '@/shared/components/Alert/Alert';
import { EmptyState } from '@/shared/components/EmptyState/EmptyState';
import { getErrorMessage } from '@/shared/utils/getErrorMessage';
import type { Property } from '@/features/property-mngt/properties/types/property.types';

export function PropertyLotsTab() {
  const { property } = useOutletContext<{ property: Property }>();
  const buildings = useBuildings(property.id);
  const showShares = property.duesCalculationMode === 'SHARES';
  // L'immeuble ouvert, jamais imposé par un effet : la sélection est relue à
  // chaque rendu contre la liste réelle, si bien qu'un immeuble supprimé ou une
  // liste rechargée ramène simplement au premier au lieu d'ouvrir sur du vide.
  const [openBuildingId, setOpenBuildingId] = useState<string | null>(null);

  if (buildings.isLoading) {
    return <Loader label="Chargement des immeubles…" />;
  }

  const allBuildings = buildings.data?.content ?? [];
  const openBuilding = allBuildings.find((building) => building.id === openBuildingId) ?? allBuildings[0];
  const hasSeveralBuildings = allBuildings.length > 1;

  return (
    <div className="flex flex-col gap-4">
      {/* En tête, à droite : « Ajouter un immeuble » se lit sur la même ligne
          d'action que « Modifier l'immeuble » et « Ajouter un lot » des cartes
          en dessous, plutôt qu'isolé tout en bas de la page.
          Un lien, pas un dépliant : la saisie a sa propre page
          (AddBuildingPage), ce qui lui rend une adresse, le retour arrière et
          l'ouverture dans un onglet. */}
      <div className="flex items-center justify-between gap-3">
        <h2 className="font-medium text-gray-900 dark:text-white/90">Immeubles</h2>
        <Link
          to={`/property-mngt/properties/${property.id}/buildings/new`}
          className="inline-flex min-h-11 shrink-0 items-center justify-center gap-2 rounded-lg bg-white px-4 py-2 text-sm font-medium text-gray-700 shadow-theme-xs ring-1 ring-inset ring-gray-300 transition-colors hover:bg-gray-50 dark:bg-gray-800 dark:text-gray-400 dark:ring-gray-700 dark:hover:bg-white/[0.03] dark:hover:text-gray-300"
        >
          Ajouter un immeuble
        </Link>
      </div>
      {buildings.isError && <Alert message={getErrorMessage(buildings.error)} />}
      {buildings.data && allBuildings.length === 0 && (
        <EmptyState title="Aucun immeuble pour le moment">
          Cette copropriété ne contient pas encore d'immeuble.
        </EmptyState>
      )}

      {/* Un immeuble à la fois, sous forme d'onglets : empilés, deux immeubles de
          trente lots faisaient défiler la page entière pour atteindre le second,
          et chaque liste emportait ses propres filtres et sa propre pagination
          dans ce défilement. Un seul immeuble ne mérite pas d'onglet - il n'y a
          rien à choisir. Défile latéralement plutôt que de passer à la ligne sur
          un écran étroit, comme les onglets d'une assemblée.
          aria-current plutôt que role="tab" : ces boutons se parcourent à la
          touche Tab comme la barre d'onglets d'une assemblée (NavLink), là où un
          vrai tablist promettrait une navigation aux flèches qu'il faudrait
          alors implémenter pour de bon. */}
      {hasSeveralBuildings && (
        <nav aria-label="Immeubles" className="-mx-1 flex gap-1 overflow-x-auto border-b border-gray-200 dark:border-gray-800">
          {allBuildings.map((building) => {
            const isOpen = building.id === openBuilding?.id;
            return (
              <button
                key={building.id}
                type="button"
                aria-current={isOpen}
                onClick={() => setOpenBuildingId(building.id)}
                className={`-mb-px shrink-0 border-b-2 px-3 py-2 text-sm font-medium transition-colors ${
                  isOpen
                    ? 'border-brand-500 text-brand-500'
                    : 'border-transparent text-gray-500 dark:text-gray-400 hover:text-gray-700 dark:hover:text-gray-300'
                }`}
              >
                {building.name}
              </button>
            );
          })}
        </nav>
      )}

      {openBuilding && <BuildingSection key={openBuilding.id} building={openBuilding} showShares={showShares} />}
    </div>
  );
}
