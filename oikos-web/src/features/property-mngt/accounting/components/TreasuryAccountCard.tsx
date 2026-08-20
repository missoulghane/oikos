import { CardLink } from '@/shared/components/Card/CardLink';
import { ACCOUNT_ROLE_LABELS, getTreasuryBalanceColorClass } from '@/features/property-mngt/accounting/constants/accountingLabels';
import type { LedgerAccount } from '@/features/property-mngt/accounting/types/accounting.types';

export function TreasuryAccountCard({ account, propertyId }: { account: LedgerAccount; propertyId: string }) {
  return (
    <CardLink
      to={`/property-mngt/properties/${propertyId}/accounting/treasury-accounts/${account.id}`}
      className="flex flex-col gap-2"
    >
      <p className="text-sm text-gray-500 dark:text-gray-400">{account.role ? ACCOUNT_ROLE_LABELS[account.role] : 'Compte'}</p>
      <p className="text-sm font-medium text-gray-900 dark:text-white/90">
        {account.accountNumber} — {account.label}
      </p>
      {/* RIB/IBAN : porté par les comptes de banque seulement, la caisse n'en a pas. */}
      {account.bankAccountNumber && (
        <p className="text-xs text-gray-500 dark:text-gray-400">N° de compte : {account.bankAccountNumber}</p>
      )}
      <p className={`text-2xl font-semibold ${getTreasuryBalanceColorClass(account.balance)}`}>
        {account.balance.toLocaleString('fr-FR')} MAD
      </p>
    </CardLink>
  );
}
