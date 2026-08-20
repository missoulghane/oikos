import { hasFallenDue, isDueBy } from '@/features/property-ownership/installments/utils/installmentTotals';
import { todayIsoDate } from '@/shared/utils/todayIsoDate';
import type { InstallmentStatus } from '@/features/property-mngt/installments/types/installment.types';

type Called = { unitId: string; dueDate: string; amount: number };
type Paid = { unitId: string; amount: number };
type Owed = { unitId: string; status: InstallmentStatus; dueDate: string };

/**
 * Le solde du compte d'un lot : tout ce que le copropriétaire a versé, moins
 * tout ce qui lui a été appelé et est échu. Positif = il a de l'avance,
 * négatif = il doit.
 *
 * C'est un compte courant, pas un cumul de restes à payer : une seule ligne
 * peut rester « non soldée » alors que le lot est créditeur, simplement parce
 * que l'avance n'a pas encore été imputée dessus (voir « Régulariser les
 * avances »). Le copropriétaire, lui, ne doit rien - et c'est ce chiffre-là
 * qu'il vient chercher.
 *
 * Deux précautions dans la formule :
 * - on somme `amount`, jamais `outstandingAmount` : le second est déjà net des
 *   paiements imputés, les additionner aux versements compterait deux fois.
 * - seules les échéances **échues** entrent au débit : un appel de septembre ne
 *   pèse pas sur le solde du mois d'août (voir hasFallenDue). Son statut n'y
 *   change rien, réglé d'avance ou non.
 */
export function unitAccountBalance(
  installments: readonly Called[],
  payments: readonly Paid[],
  unitId: string,
  asOf: string = todayIsoDate(),
): number {
  const paid = payments
    .filter((payment) => payment.unitId === unitId)
    .reduce((sum, payment) => sum + payment.amount, 0);
  const called = installments
    .filter((installment) => installment.unitId === unitId && hasFallenDue(installment, asOf))
    .reduce((sum, installment) => sum + installment.amount, 0);
  return paid - called;
}

/**
 * How many echeances of the lot are still to pay at the reference date. Not
 * what the balance is made of - the balance is a running account, this is the
 * count of lines left unsettled - but what the sentence under a debit balance
 * names, so it uses the same due-date cutoff.
 */
export function unitDueCount(installments: readonly Owed[], unitId: string, asOf?: string): number {
  return installments.filter((installment) => installment.unitId === unitId && isDueBy(installment, asOf)).length;
}

/**
 * Où mène le solde d'un lot : son relevé de compte, quel que soit le signe. Un
 * chiffre doit mener à ce qui le compose, et le relevé est le seul écran qui
 * porte les deux côtés - les échéances à régler ne montreraient rien à un lot
 * créditeur, les versements rien à un lot qui n'a jamais payé.
 */
export function unitStatementLink(propertyId: string, unitId: string): string {
  return `/property-ownership/units/${propertyId}/${unitId}/statement`;
}

/**
 * Same palette as the accounting module's getTreasuryBalanceColorClass, and now
 * the same polarity too: this is an account balance, so being in the red is the
 * bad case and an advance is not. Only a strictly negative balance is painted
 * as a debt - zero is being up to date, not a warning.
 */
export function getUnitBalanceColorClass(balance: number): string {
  if (balance < 0) {
    return 'text-error-600 dark:text-error-400';
  }
  return 'text-success-600 dark:text-success-500';
}
