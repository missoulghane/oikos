import { Badge } from '@/shared/components/Badge/Badge';
import { ACCOUNT_NATURE_LABELS, ACCOUNT_ROLE_LABELS } from '@/features/property-mngt/accounting/constants/accountingLabels';
import type { LedgerAccount } from '@/features/property-mngt/accounting/types/accounting.types';

export function LedgerAccountRow({ account }: { account: LedgerAccount }) {
  return (
    <li className="flex flex-col gap-1 px-3 py-2 sm:flex-row sm:items-center sm:justify-between">
      <div>
        <p className="text-sm font-medium text-gray-900 dark:text-white/90">
          {account.accountNumber} — {account.label}
        </p>
        <p className="text-sm text-gray-500 dark:text-gray-400">
          {ACCOUNT_NATURE_LABELS[account.nature]}
          {account.role && ` · ${ACCOUNT_ROLE_LABELS[account.role]}`}
          {account.unitId && ' · Lot'}
          {account.collective && ' · Compte collectif'}
        </p>
      </div>
      <div className="flex shrink-0 items-center gap-2">
        <span className="text-sm font-medium text-gray-900 dark:text-white/90">
          {account.balance.toLocaleString('fr-FR')} MAD
        </span>
        {!account.active && <Badge color="light">Inactif</Badge>}
      </div>
    </li>
  );
}
