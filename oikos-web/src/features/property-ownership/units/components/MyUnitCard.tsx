import { CardLink } from '@/shared/components/Card/CardLink';
import { UnitBalanceSummary } from '@/features/property-ownership/units/components/UnitBalanceSummary';
import type { OwnedUnit } from '@/features/property-ownership/units/types/unit.types';

interface MyUnitCardProps {
  unit: OwnedUnit;
  /** Le solde du compte du lot; undefined tant que les échéances ou les versements chargent. */
  balance?: number;
  /** Combien d'échéances échues restent à régler - voir unitDueCount. */
  dueCount?: number;
}

/**
 * Une seule destination, la carte entière : la fiche du lot. Le solde y est
 * annoncé, pas ouvert - c'est le badge de la fiche qui mène au relevé. Deux
 * cibles sur une même carte feraient hésiter là où il n'y a qu'un geste.
 *
 * CardLink et non Link+Card : c'est lui qui porte l'ombre au survol, la même
 * que sur toutes les cartes cliquables des tableaux de bord. Sans lui, ces
 * cartes-ci ne réagissaient pas du tout.
 */
export function MyUnitCard({ unit, balance, dueCount = 0 }: MyUnitCardProps) {
  return (
    <CardLink to={`/property-ownership/units/${unit.propertyId}/${unit.unitId}`} className="flex flex-col gap-3">
      <div className="flex flex-col gap-1">
        <h2 className="font-medium text-gray-900 dark:text-white/90">{unit.propertyName}</h2>
        <p className="text-sm text-gray-500 dark:text-gray-400">
          {unit.buildingName} — {unit.unitNumber}
        </p>
      </div>
      {/* Le même bloc que sur la fiche du lot, au même format : c'est le même
          solde, et deux tailles pour un même chiffre le font relire deux fois. */}
      {balance !== undefined && <UnitBalanceSummary balance={balance} dueCount={dueCount} />}
    </CardLink>
  );
}
