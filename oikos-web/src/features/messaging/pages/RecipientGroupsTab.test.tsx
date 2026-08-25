import { describe, expect, it, vi } from 'vitest';
import { render, screen } from '@testing-library/react';
import { MemoryRouter, Route, Routes } from 'react-router-dom';
import type { RecipientGroup } from '@/features/messaging/types/messaging.types';

const property = { id: 'property-1', name: 'Al Amal' };

const groups: RecipientGroup[] = [
  {
    id: 'group-1',
    propertyId: 'property-1',
    name: 'Habitants du bâtiment 1',
    members: [
      { userId: 'user-1', fullName: 'Alice Bennani' },
      { userId: 'user-2', fullName: 'Karim Tazi' },
    ],
  },
];

// Mutable : un test rejoue l'écran en simple copropriétaire.
let boardTier = true;

vi.mock('@/features/identity/me', () => ({
  useCurrentUser: () => ({ data: { id: 'user-9' } }),
  isBoardTierOnProperty: () => boardTier,
  isManagerTierOnProperty: () => false,
}));
vi.mock('@/features/messaging/hooks/useRecipientGroups', () => ({
  useRecipientGroups: () => ({ data: groups, isLoading: false, isError: false, error: null }),
  useCreateRecipientGroup: () => ({ mutate: vi.fn(), isPending: false, error: null }),
  useUpdateRecipientGroup: () => ({ mutate: vi.fn(), isPending: false, error: null }),
  useDeleteRecipientGroup: () => ({ mutate: vi.fn(), isPending: false, error: null, variables: undefined }),
}));
// Le sélecteur tire l'annuaire de la copropriété ; il a le sien de test.
vi.mock('@/features/messaging/components/RecipientPicker', () => ({
  RecipientPicker: () => <div data-testid="recipient-picker" />,
}));

const { RecipientGroupsTab } = await import('@/features/messaging/pages/RecipientGroupsTab');

function renderTab() {
  return render(
    <MemoryRouter initialEntries={['/groups']}>
      <Routes>
        <Route path="/groups" element={<RecipientGroupsTab />} />
      </Routes>
    </MemoryRouter>,
  );
}

vi.mock('react-router-dom', async () => {
  const actual = await vi.importActual<typeof import('react-router-dom')>('react-router-dom');
  return { ...actual, useOutletContext: () => ({ property }) };
});

describe('RecipientGroupsTab', () => {
  it('lists each group with the people it holds', () => {
    boardTier = true;
    renderTab();

    expect(screen.getByText('Habitants du bâtiment 1')).toBeInTheDocument();
    expect(screen.getByText('2 membres')).toBeInTheDocument();
    expect(screen.getByText('Alice Bennani, Karim Tazi')).toBeInTheDocument();
  });

  it('lets a board member create and edit groups', () => {
    boardTier = true;
    renderTab();

    expect(screen.getByRole('button', { name: 'Créer le groupe' })).toBeInTheDocument();
    expect(screen.getByRole('button', { name: 'Modifier' })).toBeInTheDocument();
    expect(screen.getByRole('button', { name: 'Supprimer' })).toBeInTheDocument();
  });

  // Voir les groupes qu'on peut utiliser en composant est utile ; les tenir est
  // réservé au bureau, côté API comme ici.
  it('shows a plain owner the groups without any way to change them', () => {
    boardTier = false;
    renderTab();

    expect(screen.getByText('Habitants du bâtiment 1')).toBeInTheDocument();
    expect(screen.queryByRole('button', { name: 'Créer le groupe' })).not.toBeInTheDocument();
    expect(screen.queryByRole('button', { name: 'Modifier' })).not.toBeInTheDocument();
    expect(screen.queryByRole('button', { name: 'Supprimer' })).not.toBeInTheDocument();
  });
});
