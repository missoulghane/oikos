import { useState } from 'react';
import { Button } from '@/shared/components/Button/Button';
import { SectionBlock } from '@/shared/components/SectionBlock/SectionBlock';
import { EditUnitForm } from '@/features/property-mngt/properties/components/EditUnitForm';
import { floorLabel } from '@/features/property-mngt/properties/utils/floorLabel';
import type { Building, Unit } from '@/features/property-mngt/properties/types/property.types';

const OWNERSHIP_STATUS_LABELS = {
  AFFECTED: 'Affecté',
  NOT_AFFECTED: 'Non affecté',
} as const;

interface UnitInfoSectionProps {
  unit: Unit;
  /** Arrive après le lot : tant qu'il manque, l'immeuble s'affiche « … » et la modification attend. */
  building: Building | undefined;
  /** Les tantièmes ne se lisent - et ne se saisissent - qu'en mode tantièmes. */
  showShares: boolean;
}

function InfoRow({ label, value }: { label: string; value: string }) {
  return (
    <div>
      <dt className="text-sm text-gray-500 dark:text-gray-400">{label}</dt>
      <dd className="text-gray-900 dark:text-white/90">{value}</dd>
    </div>
  );
}

/**
 * Ce qu'est le lot, en premier bloc de sa fiche et sur le même modèle que les
 * blocs suivants (Propriétaires, Échéances, Paiements) : titre, action, trait.
 *
 * Un champ vide reste affiché (« Non renseigné ») plutôt que de disparaître : les
 * lots générés à la configuration n'ont pas d'étage, et une ligne absente ne dit
 * pas qu'il reste quelque chose à compléter. La modification se fait ici même,
 * le formulaire prenant la place des informations le temps de la saisie.
 */
export function UnitInfoSection({ unit, building, showShares }: UnitInfoSectionProps) {
  const [isEditing, setIsEditing] = useState(false);

  const editButton = !isEditing ? (
    <Button type="button" variant="secondary" disabled={!building} onClick={() => setIsEditing(true)}>
      Modifier le lot
    </Button>
  ) : null;

  return (
    <SectionBlock title="Informations du lot" action={editButton}>
      {isEditing && building ? (
        <div className="max-w-md">
          <EditUnitForm
            unit={unit}
            building={building}
            showShares={showShares}
            onSuccess={() => setIsEditing(false)}
            onCancel={() => setIsEditing(false)}
          />
        </div>
      ) : (
        <dl className="grid grid-cols-1 gap-3 sm:grid-cols-3">
          <InfoRow label="Numéro" value={unit.unitNumber} />
          <InfoRow label="Type" value={unit.unitTypeName} />
          <InfoRow label="Immeuble" value={building ? building.name : '…'} />
          <InfoRow label="Étage" value={typeof unit.floor === 'number' ? floorLabel(unit.floor) : 'Non renseigné'} />
          {showShares && <InfoRow label="Tantièmes" value={`${unit.shares}`} />}
          <InfoRow label="Affectation" value={OWNERSHIP_STATUS_LABELS[unit.ownershipStatus]} />
        </dl>
      )}
    </SectionBlock>
  );
}
