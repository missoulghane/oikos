import { MOVEMENT_TYPE_LABELS } from '@/features/property-mngt/accounting/constants/movementLabels';
import type { Movement } from '@/features/property-mngt/accounting/types/accounting.types';

export function MovementList({ movements }: { movements: Movement[] }) {
  return (
    <ul className="flex flex-col divide-y divide-slate-200 rounded-md border border-slate-200">
      {movements.map((movement) => (
        <li key={movement.id} className="flex items-center justify-between gap-2 px-3 py-2">
          <div>
            <p className="text-sm font-medium text-slate-900">{movement.label}</p>
            <p className="text-sm text-slate-500">
              {new Date(movement.occurredOn).toLocaleDateString('fr-FR')} · {MOVEMENT_TYPE_LABELS[movement.type]}
            </p>
          </div>
          <span
            className={`text-sm font-medium ${movement.direction === 'CREDIT' ? 'text-green-700' : 'text-red-700'}`}
          >
            {movement.direction === 'CREDIT' ? '+' : '−'}
            {movement.amount} MAD
          </span>
        </li>
      ))}
    </ul>
  );
}
