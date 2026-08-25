import { beforeEach, describe, expect, it, vi } from 'vitest';
import { render, screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { RecipientPicker } from '@/features/messaging/components/RecipientPicker';
import { listRecipientCandidates } from '@/features/messaging/api/listRecipientCandidates';
import { getProperty } from '@/features/property-mngt/properties/api/getProperty';
import type { RecipientCandidate } from '@/features/messaging/types/messaging.types';
import type { Property } from '@/features/property-mngt/properties/types/property.types';

// Factory form (not bare automock): a bare `vi.mock(path)` still loads the
// real module to derive its shape, which would drag in httpClient -> env.ts
// (VITE_API_URL) with no .env available under Vitest.
vi.mock('@/features/messaging/api/listRecipientCandidates', () => ({
  listRecipientCandidates: vi.fn(),
}));
vi.mock('@/features/property-mngt/properties/api/getProperty', () => ({
  getProperty: vi.fn(),
}));

const mockedListRecipientCandidates = vi.mocked(listRecipientCandidates);
const mockedGetProperty = vi.mocked(getProperty);

const property: Property = {
  id: 'property-1',
  name: 'Résidence Al Amal',
  address: '',
  city: 'Casablanca',
  duesCalculationMode: 'FLAT_RATE',
  projectedBudget: null,
};

const jean: RecipientCandidate = {
  userId: 'user-1',
  fullName: 'Jean Dupont',
  roleLabel: 'Copropriétaire',
  unitNumbers: ['12B'],
  isStaff: false,
};
const marie: RecipientCandidate = {
  userId: 'user-2',
  fullName: 'Marie Curie',
  roleLabel: 'Gérant',
  unitNumbers: [],
  isStaff: true,
};

function renderPicker(
  value: RecipientCandidate[],
  options: { canBroadcast?: boolean; canBoardPrivate?: boolean } = {},
) {
  const queryClient = new QueryClient({ defaultOptions: { queries: { retry: false } } });
  const onChange = vi.fn();
  render(
    <QueryClientProvider client={queryClient}>
      <RecipientPicker
        propertyId="property-1"
        value={value}
        onChange={onChange}
        canBroadcast={options.canBroadcast}
        canBoardPrivate={options.canBoardPrivate}
      />
    </QueryClientProvider>,
  );
  return { onChange };
}

describe('RecipientPicker', () => {
  beforeEach(() => {
    mockedListRecipientCandidates.mockReset();
    mockedListRecipientCandidates.mockResolvedValue([jean, marie]);
    mockedGetProperty.mockReset();
    mockedGetProperty.mockResolvedValue(property);
  });

  it('shows no candidate and does not query until the user types something (autocomplete, not a browse-all list)', async () => {
    renderPicker([]);

    // Give any debounce/microtask a chance to run, then assert nothing fired.
    await new Promise((resolve) => setTimeout(resolve, 350));
    expect(mockedListRecipientCandidates).not.toHaveBeenCalled();
    expect(screen.queryByText('Jean Dupont')).not.toBeInTheDocument();
    expect(screen.queryByText('Marie Curie')).not.toBeInTheDocument();
  });

  it('queries and shows candidates - name with lot, property with role - once the user starts typing', async () => {
    const user = userEvent.setup();
    renderPicker([]);

    await user.type(screen.getByLabelText('Ajouter un destinataire'), 'Jean');

    await waitFor(() => expect(mockedListRecipientCandidates).toHaveBeenCalled());
    expect(await screen.findByText('Jean Dupont - 12B')).toBeInTheDocument();
    expect(await screen.findByText('Résidence Al Amal - Copropriétaire')).toBeInTheDocument();
    // Marie owns no unit here (board/manager seat) - no "- Lot" suffix.
    expect(await screen.findByText('Marie Curie')).toBeInTheDocument();
  });

  it('adds a clicked candidate to the selection instead of navigating', async () => {
    const user = userEvent.setup();
    const { onChange } = renderPicker([]);

    await user.type(screen.getByLabelText('Ajouter un destinataire'), 'Jean');
    await user.click(await screen.findByText('Jean Dupont - 12B'));

    expect(onChange).toHaveBeenCalledWith([jean]);
  });

  it('filters already-selected candidates out of the search results', async () => {
    const user = userEvent.setup();
    renderPicker([jean]);

    await user.type(screen.getByLabelText('Ajouter un destinataire'), 'a');

    await waitFor(() => expect(mockedListRecipientCandidates).toHaveBeenCalled());
    expect(await screen.findByText('Marie Curie')).toBeInTheDocument();
    // Jean Dupont should render exactly once - as the chip, not again in the results list.
    expect(screen.getAllByText('Jean Dupont - 12B')).toHaveLength(1);
  });

  it('removes a recipient when its chip × button is clicked', async () => {
    const user = userEvent.setup();
    const { onChange } = renderPicker([jean, marie]);

    await user.click(screen.getByRole('button', { name: 'Retirer Jean Dupont' }));

    expect(onChange).toHaveBeenCalledWith([marie]);
  });

  it('does not offer "Le bureau" when the sender is not staff on this property', () => {
    renderPicker([], { canBoardPrivate: false });

    expect(screen.queryByRole('button', { name: 'Écrire au conseil (fil privé)' })).not.toBeInTheDocument();
  });

  it('selecting "Le bureau" sets it as the sole recipient and hides the search field', async () => {
    const user = userEvent.setup();
    const { onChange } = renderPicker([], { canBoardPrivate: true });

    await user.click(screen.getByRole('button', { name: 'Écrire au conseil (fil privé)' }));

    expect(onChange).toHaveBeenCalledWith([
      { userId: '__board__', fullName: 'Le conseil syndical', roleLabel: 'Fil privé', unitNumbers: [], isStaff: true },
    ]);
  });

  it('hides the search field once "Le bureau" is the selected recipient', () => {
    renderPicker(
      [{ userId: '__board__', fullName: 'Le conseil syndical', roleLabel: 'Fil privé', unitNumbers: [], isStaff: true }],
      { canBoardPrivate: true },
    );

    expect(screen.queryByLabelText('Ajouter un destinataire')).not.toBeInTheDocument();
  });
});
