import { Link, useNavigate, useParams } from 'react-router-dom';
import { useUnitInstallments } from '@/features/property-mngt/installments/hooks/useUnitInstallments';
import { useUnitPayments } from '@/features/property-mngt/installments/hooks/useUnitPayments';
import { useMyUnits } from '@/features/property-ownership/units/hooks/useMyUnits';
import { formatUnitLabel } from '@/features/property-ownership/units/utils/formatUnitLabel';
import { buildAccountStatement, statementTotals } from '@/features/property-ownership/units/utils/accountStatement';
import { getUnitBalanceColorClass } from '@/features/property-ownership/units/utils/unitBalance';
import { Card } from '@/shared/components/Card/Card';
import { Loader } from '@/shared/components/Loader/Loader';
import { Alert } from '@/shared/components/Alert/Alert';
import { EmptyState } from '@/shared/components/EmptyState/EmptyState';
import { getErrorMessage } from '@/shared/utils/getErrorMessage';

const formatAmount = (amount: number) => `${amount.toLocaleString('fr-FR')} MAD`;
const formatDate = (iso: string) => new Date(iso).toLocaleDateString('fr-FR');

/**
 * Le relevé de compte d'un lot : ce qui a été appelé et échu au débit, ce qui a
 * été versé au crédit, dans l'ordre chronologique, et le solde en pied.
 *
 * C'est là que mène le badge de solde, sur le tableau de bord comme sur la
 * fiche du lot : un chiffre doit mener à ce qui le compose.
 */
export function MyUnitStatementPage() {
  const { propertyId, unitId } = useParams<{ propertyId: string; unitId: string }>();
  const navigate = useNavigate();
  const id = unitId ?? '';
  // Mêmes clés de cache que la fiche du lot : y arriver depuis le badge ne
  // relance aucune requête.
  const installments = useUnitInstallments(id);
  const payments = useUnitPayments(id);
  const myUnits = useMyUnits();

  const isLoading = installments.isLoading || payments.isLoading;
  const error = installments.error ?? payments.error;

  const ownedUnit = (myUnits.data ?? []).find((candidate) => candidate.unitId === id);
  const lotLabel = ownedUnit ? formatUnitLabel(ownedUnit) : '—';
  const lines = buildAccountStatement(installments.data ?? [], payments.data ?? [], id);
  const totals = statementTotals(lines);

  return (
    <div className="flex flex-col gap-6">
      <div>
        <Link
          to={`/property-ownership/units/${propertyId}/${id}`}
          className="text-sm text-gray-500 dark:text-gray-400 hover:underline"
        >
          ← Retour au lot
        </Link>
        <h1 className="text-lg font-semibold text-gray-900 dark:text-white/90">Relevé de compte</h1>
        {ownedUnit && <p className="text-sm text-gray-500 dark:text-gray-400">{lotLabel}</p>}
      </div>

      <Card className="flex flex-col gap-4">
        <p className="text-sm text-gray-500 dark:text-gray-400">
          Les opérations qui ont réellement mouvementé votre compte. Une échéance à venir n'y figure pas : elle y
          entrera à sa date.
        </p>

        {isLoading && <Loader label="Chargement du relevé…" />}
        {error && <Alert message={getErrorMessage(error)} />}

        {!isLoading && !error && lines.length === 0 && (
          <EmptyState title="Aucune opération">
            Rien n'a encore été appelé ni versé sur ce lot.
          </EmptyState>
        )}

        {lines.length > 0 && (
          <div className="overflow-x-auto">
            <table className="min-w-full divide-y divide-gray-200 dark:divide-gray-800 text-sm">
              <thead>
                <tr className="text-left text-gray-500 dark:text-gray-400">
                  <th className="py-2 pr-4 font-medium">Date d'opération</th>
                  <th className="py-2 pr-4 font-medium">Nature</th>
                  <th className="py-2 pr-4 font-medium">Lot</th>
                  <th className="py-2 pr-4 text-right font-medium">Débit</th>
                  <th className="py-2 text-right font-medium">Crédit</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-gray-100 dark:divide-gray-800">
                {lines.map((line) => (
                  // Toute la ligne est cliquable, comme sur Mes échéances, mais
                  // la nature porte un vrai lien : c'est lui qui rend la ligne
                  // atteignable au clavier, là où un onClick sur le <tr> ne
                  // l'est pas. Les deux mènent au même écran.
                  <tr
                    key={line.id}
                    onClick={() => navigate(line.to)}
                    className="cursor-pointer hover:bg-gray-50 dark:hover:bg-white/[0.03]"
                  >
                    <td className="py-2 pr-4 text-gray-700 dark:text-gray-300">{formatDate(line.date)}</td>
                    <td className="py-2 pr-4">
                      <Link to={line.to} className="text-gray-700 hover:underline dark:text-gray-300">
                        {line.label}
                      </Link>
                    </td>
                    <td className="py-2 pr-4 text-gray-500 dark:text-gray-400">{lotLabel}</td>
                    {/* Une colonne ou l'autre, jamais les deux : une opération
                        débite ou crédite, et une cellule vide se lit plus vite
                        qu'un zéro à écarter. */}
                    <td className="py-2 pr-4 text-right text-gray-700 dark:text-gray-300">
                      {line.debit > 0 ? formatAmount(line.debit) : ''}
                    </td>
                    <td className="py-2 text-right text-gray-700 dark:text-gray-300">
                      {line.credit > 0 ? formatAmount(line.credit) : ''}
                    </td>
                  </tr>
                ))}
              </tbody>
              <tfoot>
                <tr className="border-t border-gray-200 dark:border-gray-800 font-medium text-gray-900 dark:text-white/90">
                  <td className="py-2 pr-4" colSpan={3}>
                    Totaux
                  </td>
                  <td className="py-2 pr-4 text-right">{formatAmount(totals.debit)}</td>
                  <td className="py-2 text-right">{formatAmount(totals.credit)}</td>
                </tr>
                <tr className="font-medium text-gray-900 dark:text-white/90">
                  <td className="py-2 pr-4" colSpan={3}>
                    Solde du compte
                  </td>
                  {/* Le solde tient sur les deux colonnes de montants : il n'est
                      ni au débit ni au crédit, c'est ce qui reste des deux. */}
                  <td className={`py-2 text-right text-base ${getUnitBalanceColorClass(totals.balance)}`} colSpan={2}>
                    {formatAmount(totals.balance)}
                  </td>
                </tr>
              </tfoot>
            </table>
          </div>
        )}
      </Card>
    </div>
  );
}
