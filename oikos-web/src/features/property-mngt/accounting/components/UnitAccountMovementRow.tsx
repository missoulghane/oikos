import { Badge } from '@/shared/components/Badge/Badge';
import {
  UNIT_ACCOUNT_MOVEMENT_DIRECTION_BADGE_COLORS,
  UNIT_ACCOUNT_MOVEMENT_DIRECTION_LABELS,
  UNIT_ACCOUNT_MOVEMENT_TYPE_LABELS,
} from '@/features/property-mngt/accounting/constants/unitAccountMovementLabels';
import type { UnitAccountMovement } from '@/features/property-mngt/accounting/types/accounting.types';

interface UnitAccountMovementRowProps {
  movement: UnitAccountMovement;
}

export function UnitAccountMovementRow({ movement }: UnitAccountMovementRowProps) {
  return (
    <li className="flex flex-col gap-1 px-3 py-2 sm:flex-row sm:items-center sm:justify-between">
      <div>
        <div className="flex items-center gap-2">
          <p className="text-sm font-medium text-gray-900">{UNIT_ACCOUNT_MOVEMENT_TYPE_LABELS[movement.type]}</p>
          <Badge color={UNIT_ACCOUNT_MOVEMENT_DIRECTION_BADGE_COLORS[movement.direction]}>
            {UNIT_ACCOUNT_MOVEMENT_DIRECTION_LABELS[movement.direction]}
          </Badge>
        </div>
        <p className="text-sm text-gray-500">
          {new Date(movement.date).toLocaleDateString('fr-FR')} · {movement.label}
          {movement.reason ? ` · ${movement.reason}` : ''}
        </p>
      </div>
      <p className="text-sm font-medium text-gray-900">{movement.amount.toLocaleString('fr-FR')} MAD</p>
    </li>
  );
}
