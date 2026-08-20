import { getUnitBalanceColorClass } from '@/features/property-ownership/units/utils/unitBalance';

interface UnitBalanceSummaryProps {
  /** Le solde du compte du lot : versements moins appels échus, négatif quand le lot doit. */
  balance: number;
  /** Combien d'échéances échues restent non soldées - ce que nomme la phrase sous un solde débiteur. */
  dueCount: number;
}

/**
 * Le solde d'un lot, tel qu'il se lit partout dans l'espace copropriétaire : le
 * libellé, le montant signé, sa couleur, et la phrase qui dit ce qu'il
 * recouvre. Un composant plutôt que deux mises en forme, parce qu'un solde qui
 * se lirait autrement sur la carte du lot que sur sa fiche serait un solde
 * qu'on va vérifier ailleurs.
 *
 * Le montant est toujours affiché, même à zéro : « À jour » seul laissait le
 * copropriétaire sans le chiffre qui le prouve.
 */
export function UnitBalanceSummary({ balance, dueCount }: UnitBalanceSummaryProps) {
  return (
    <div className="flex flex-col gap-1">
      <span className="text-sm text-gray-500 dark:text-gray-400">Solde du compte</span>
      <span className={`text-2xl font-semibold ${getUnitBalanceColorClass(balance)}`}>
        {/* Signé comme un relevé de compte : le signe vient du solde lui-même,
            négatif quand le lot doit, sans « + » sur un crédit. */}
        {balance.toLocaleString('fr-FR')} MAD
      </span>
      <span className="text-sm text-gray-500 dark:text-gray-400">{balanceMessage(balance, dueCount)}</span>
    </div>
  );
}

/**
 * Un solde créditeur ne parle pas d'échéances : une ligne peut y rester non
 * soldée en attendant que l'avance lui soit imputée, et annoncer « 1 échéance à
 * régler » à quelqu'un qui a payé d'avance serait faux.
 */
function balanceMessage(balance: number, dueCount: number): string {
  if (balance >= 0) {
    return balance > 0 ? 'À jour · vous avez une avance' : 'À jour';
  }
  return dueCount > 0 ? `${dueCount} échéance${dueCount > 1 ? 's' : ''} à régler` : 'Montant à régler';
}
