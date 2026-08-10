import { Link } from 'react-router-dom';
import { useUnit } from '@/features/property-mngt/properties/hooks/useUnit';
import type { LedgerAccount } from '@/features/property-mngt/accounting/types/accounting.types';

export function UnitLedgerAccountRow({ account, propertyId }: { account: LedgerAccount; propertyId: string }) {
  const unit = useUnit(account.unitId);

  return (
    <li className="flex flex-col gap-1 px-3 py-2 sm:flex-row sm:items-center sm:justify-between">
      <div>
        {unit.data ? (
          <Link
            to={`/property-mngt/properties/${propertyId}/units/${unit.data.id}`}
            className="text-sm font-medium text-brand-500 hover:underline"
          >
            Lot {unit.data.unitNumber}
          </Link>
        ) : (
          <p className="text-sm font-medium text-gray-900">Lot</p>
        )}
        <p className="text-sm text-gray-500">
          {account.accountNumber} — {account.label}
        </p>
      </div>
      <span className="shrink-0 text-sm font-medium text-gray-900">
        {account.balance.toLocaleString('fr-FR')} MAD
      </span>
    </li>
  );
}
