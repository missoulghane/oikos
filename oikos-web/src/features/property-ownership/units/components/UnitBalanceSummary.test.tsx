import { describe, expect, it } from 'vitest';
import { render, screen } from '@testing-library/react';
import { UnitBalanceSummary } from '@/features/property-ownership/units/components/UnitBalanceSummary';

describe('UnitBalanceSummary', () => {
  it('names what the figure is, and states it even when nothing is owed', () => {
    render(<UnitBalanceSummary balance={0} dueCount={0} />);

    expect(screen.getByText('Solde du compte')).toBeInTheDocument();
    expect(screen.getByText('0 MAD')).toBeInTheDocument();
    expect(screen.getByText('À jour')).toBeInTheDocument();
  });

  it('names the echeances behind a debit balance', () => {
    render(<UnitBalanceSummary balance={-1200} dueCount={2} />);

    expect(screen.getByText('-1 200 MAD')).toBeInTheDocument();
    expect(screen.getByText('-1 200 MAD').className).toMatch(/error/);
    expect(screen.getByText('2 échéances à régler')).toBeInTheDocument();
  });

  // Le cas que « reste à régler » ne savait pas dire : quelqu'un qui a versé
  // plus que ce qui lui a été appelé n'est pas à zéro, il a une avance.
  it('shows a credit balance as such rather than bottoming out at zero', () => {
    render(<UnitBalanceSummary balance={1400} dueCount={0} />);

    expect(screen.getByText('1 400 MAD')).toBeInTheDocument();
    expect(screen.getByText('1 400 MAD').className).toMatch(/success/);
    expect(screen.getByText('À jour · vous avez une avance')).toBeInTheDocument();
  });

  // Une ligne peut rester non soldée alors que le lot est créditeur, le temps
  // que l'avance lui soit imputée : lui annoncer une échéance à régler serait
  // faux, il a déjà payé.
  it('never announces an echeance to pay to someone in credit', () => {
    render(<UnitBalanceSummary balance={1200} dueCount={1} />);

    expect(screen.queryByText(/à régler/)).not.toBeInTheDocument();
  });
});
