import { beforeEach, describe, expect, it, vi } from 'vitest';
import { render, screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { CloseExerciseForm } from '@/features/property-mngt/accounting/components/CloseExerciseForm';
import { useCloseExercise } from '@/features/property-mngt/accounting/hooks/useCloseExercise';

vi.mock('@/features/property-mngt/accounting/hooks/useCloseExercise', () => ({ useCloseExercise: vi.fn() }));

const mutate = vi.fn();

function setup(state: Record<string, unknown> = {}) {
  vi.mocked(useCloseExercise).mockReturnValue({
    mutate,
    isPending: false,
    isSuccess: false,
    data: undefined,
    error: null,
    ...state,
  } as never);
  render(<CloseExerciseForm propertyId="p-1" exerciseLabel="2026" />);
}

describe('CloseExerciseForm', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  it('demande confirmation avant de sceller quoi que ce soit', async () => {
    const user = userEvent.setup();
    setup();

    await user.click(screen.getByRole('button', { name: "Clôturer l'exercice" }));

    expect(screen.getByText(/Ce geste ne se défait pas/)).toBeInTheDocument();
    expect(mutate).not.toHaveBeenCalled();
  });

  it('clôture une fois la confirmation donnée', async () => {
    const user = userEvent.setup();
    setup();

    await user.click(screen.getByRole('button', { name: "Clôturer l'exercice" }));
    await user.click(screen.getByRole('button', { name: 'Confirmer la clôture' }));

    await waitFor(() => expect(mutate).toHaveBeenCalled());
  });

  it('annonce le résultat arrêté, et son sens', () => {
    setup({
      isSuccess: true,
      data: { exercise: { label: '2026' }, netResult: 290, closingEntryId: 'entry-1' },
    });

    expect(screen.getByText(/Exercice 2026 clôturé/)).toBeInTheDocument();
    expect(screen.getByText(/excédent/)).toBeInTheDocument();
  });

  it('dit « déficit » quand le résultat est négatif', () => {
    // Un signe moins seul se lit mal dans un message : le mot tranche.
    setup({
      isSuccess: true,
      data: { exercise: { label: '2026' }, netResult: -1500, closingEntryId: 'entry-1' },
    });

    expect(screen.getByText(/déficit/)).toBeInTheDocument();
  });
});
