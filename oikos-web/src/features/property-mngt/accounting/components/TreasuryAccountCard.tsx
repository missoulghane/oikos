import { Link } from 'react-router-dom';
import { Card } from '@/shared/components/Card/Card';
import { ACCOUNT_ROLE_LABELS, getTreasuryBalanceColorClass } from '@/features/property-mngt/accounting/constants/accountingLabels';
import type { LedgerAccount } from '@/features/property-mngt/accounting/types/accounting.types';

export function TreasuryAccountCard({ account, propertyId }: { account: LedgerAccount; propertyId: string }) {
  return (
    <Link
      to={`/property-mngt/properties/${propertyId}/accounting/treasury-accounts/${account.id}`}
      className="block transition-shadow hover:shadow-theme-md"
    >
      <Card className="flex flex-col gap-2">
        <p className="text-sm text-gray-500 dark:text-gray-400">{account.role ? ACCOUNT_ROLE_LABELS[account.role] : 'Compte'}</p>
        <p className="text-sm font-medium text-gray-900 dark:text-white/90">
          {account.accountNumber} — {account.label}
        </p>
        <p className={`text-2xl font-semibold ${getTreasuryBalanceColorClass(account.balance)}`}>
          {account.balance.toLocaleString('fr-FR')} MAD
        </p>
      </Card>
    </Link>
  );
}
