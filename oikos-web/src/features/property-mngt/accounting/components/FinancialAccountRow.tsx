import { Badge } from '@/shared/components/Badge/Badge';
import { FINANCIAL_ACCOUNT_TYPE_LABELS } from '@/features/property-mngt/accounting/constants/financialAccountTypeLabels';
import type { FinancialAccount } from '@/features/property-mngt/accounting/types/accounting.types';

interface FinancialAccountRowProps {
  account: FinancialAccount;
}

export function FinancialAccountRow({ account }: FinancialAccountRowProps) {
  return (
    <li className="flex flex-col gap-1 px-3 py-2 sm:flex-row sm:items-center sm:justify-between">
      <div className="flex items-center gap-2">
        <p className="text-sm font-medium text-gray-900">{account.name}</p>
        <Badge color={account.status === 'ACTIVE' ? 'success' : 'light'}>
          {account.status === 'ACTIVE' ? 'Actif' : 'Clôturé'}
        </Badge>
      </div>
      <p className="text-sm text-gray-500">
        {FINANCIAL_ACCOUNT_TYPE_LABELS[account.type]} · {account.balance.toLocaleString('fr-FR')} {account.currency}
      </p>
    </li>
  );
}
