import { beforeEach, describe, expect, it, vi } from 'vitest';
import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { BoardSeatSelect } from '@/features/identity/onboarding/components/BoardSeatSelect';
import { useBoardRoles } from '@/shared/hooks/useBoardRoles';
import { BOARD_ROLES_FIXTURE } from '@/test/referenceCatalogs';

vi.mock('@/shared/hooks/useBoardRoles', () => ({ useBoardRoles: vi.fn() }));

const refetch = vi.fn();

function catalog(state: { data?: typeof BOARD_ROLES_FIXTURE; isError?: boolean }) {
  vi.mocked(useBoardRoles).mockReturnValue({
    data: state.data,
    isError: state.isError ?? false,
    isFetching: false,
    refetch,
  } as never);
}

describe('BoardSeatSelect', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  it('offers only the seats open at sign-up', () => {
    catalog({ data: BOARD_ROLES_FIXTURE });

    render(<BoardSeatSelect name="boardRole" />);

    const options = screen.getAllByRole('option').map((option) => option.textContent);
    expect(options).toContain('Syndic bénévole');
    expect(options).not.toContain('Gestionnaire');
  });

  it('does not stay on the loader when the catalog could not be fetched, and lets the visitor retry', async () => {
    // L'API répondait 502 pendant son redémarrage : l'écran restait sur « Chargement des rôles… ».
    catalog({ isError: true });
    const user = userEvent.setup();

    render(<BoardSeatSelect name="boardRole" />);

    expect(screen.queryByText('Chargement des rôles…')).not.toBeInTheDocument();
    expect(screen.getByText(/Impossible de charger la liste des rôles/)).toBeInTheDocument();
    await user.click(screen.getByRole('button', { name: 'Réessayer' }));
    expect(refetch).toHaveBeenCalledTimes(1);
  });
});
