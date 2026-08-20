import { hasFallenDue } from '@/features/property-ownership/installments/utils/installmentTotals';
import { PAYMENT_MODE_LABELS } from '@/features/property-mngt/installments/constants/paymentModeLabels';
import { todayIsoDate } from '@/shared/utils/todayIsoDate';
import type { PaymentMode } from '@/features/property-mngt/installments/types/payment.types';

type Called = { id: string; unitId: string; dueDate: string; amount: number };
type Paid = { id: string; unitId: string; valueDate: string; amount: number; mode: PaymentMode };

/**
 * Une ligne de relevé : une opération qui a réellement mouvementé le compte du
 * lot, à une date et d'un seul côté.
 */
export interface StatementLine {
  id: string;
  /** Date d'opération : échéance pour un appel, date de valeur pour un règlement. */
  date: string;
  /** Nature de l'opération, telle qu'elle se lit sur le relevé. */
  label: string;
  /** L'écran de détail de l'opération - une ligne de relevé mène à sa pièce. */
  to: string;
  /** Ce que le lot doit - un appel de fonds échu. 0 sur une ligne de crédit. */
  debit: number;
  /** Ce que le lot a versé. 0 sur une ligne de débit. */
  credit: number;
}

export interface StatementTotals {
  debit: number;
  credit: number;
  /** Crédit moins débit : positif quand le lot a de l'avance, négatif quand il doit. */
  balance: number;
}

/**
 * Le relevé de compte d'un lot, dans l'ordre chronologique.
 *
 * N'y figure que ce qui a **réellement** mouvementé le compte : un appel de
 * fonds à venir n'y est pas, quel que soit son statut, puisque rien n'a encore
 * été débité (voir hasFallenDue). Il y entrera le jour de son échéance.
 *
 * Comme dans unitAccountBalance, un appel pèse pour son `amount` et jamais pour
 * son `outstandingAmount` : ce dernier est déjà net des règlements imputés, et
 * un relevé qui porterait les deux compterait le versement deux fois.
 *
 * Les deux tris à égalité de date - débit avant crédit, puis par id - n'ont
 * d'autre but que de rendre l'ordre stable : deux rendus du même relevé doivent
 * donner les mêmes lignes dans le même ordre.
 */
export function buildAccountStatement(
  installments: readonly Called[],
  payments: readonly Paid[],
  unitId: string,
  asOf: string = todayIsoDate(),
): StatementLine[] {
  const debits: StatementLine[] = installments
    .filter((installment) => installment.unitId === unitId && hasFallenDue(installment, asOf))
    .map((installment) => ({
      id: installment.id,
      date: installment.dueDate,
      // Pas de période dans le libellé : GET /units/{id}/installments ne la
      // résout pas (voir ListInstallmentsByUnitService), elle vaut toujours
      // null ici. Elle est sur l'écran de détail, au bout de la ligne.
      label: 'Appel de fonds',
      to: `/property-ownership/installments/${installment.id}`,
      debit: installment.amount,
      credit: 0,
    }));

  const credits: StatementLine[] = payments
    .filter((payment) => payment.unitId === unitId)
    .map((payment) => ({
      id: payment.id,
      date: payment.valueDate,
      // Le moyen de paiement fait partie de la nature de l'opération : c'est ce
      // qui distingue deux règlements du même jour sur un relevé.
      label: `Règlement — ${PAYMENT_MODE_LABELS[payment.mode]}`,
      to: `/property-ownership/payments/${payment.id}`,
      debit: 0,
      credit: payment.amount,
    }));

  return [...debits, ...credits].sort(
    (a, b) => a.date.localeCompare(b.date) || b.debit - a.debit || a.id.localeCompare(b.id),
  );
}

/**
 * Le pied du relevé. `balance` vaut exactement le solde affiché sur le badge du
 * lot (unitAccountBalance) - c'est la même soustraction, et un relevé dont le
 * total ne retomberait pas sur le badge qui y mène serait un relevé qu'on va
 * recompter à la main.
 */
export function statementTotals(lines: readonly StatementLine[]): StatementTotals {
  const debit = lines.reduce((sum, line) => sum + line.debit, 0);
  const credit = lines.reduce((sum, line) => sum + line.credit, 0);
  return { debit, credit, balance: credit - debit };
}
