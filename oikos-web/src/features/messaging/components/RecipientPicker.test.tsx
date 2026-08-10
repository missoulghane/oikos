import { beforeEach, describe, expect, it, vi } from 'vitest';
import { render, screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { RecipientPicker } from '@/features/messaging/components/RecipientPicker';
import { listRecipientCandidates } from '@/features/messaging/api/listRecipientCandidates';
import type { RecipientCandidate } from '@/features/messaging/types/messaging.types';

// Factory form (not bare automock): a bare `vi.mock(path)` still loads the
// real module to derive its shape, which would drag in httpClient -> env.ts
// (VITE_API_URL) with no .env available under Vitest.
vi.mock('@/features/messaging/api/listRecipientCandidates', () => ({
  listRecipientCandidates: vi.fn(),
}));

const mockedListRecipientCandidates = vi.mocked(listRecipientCandidates);

const jean: RecipientCandidate = { userId: 'user-1', fullName: 'Jean Dupont', roleLabel: 'Copropriétaire' };
const marie: RecipientCandidate = { userId: 'user-2', fullName: 'Marie Curie', roleLabel: 'Gérant' };

function renderPicker(value: RecipientCandidate[]) {
  const queryClient = new QueryClient({ defaultOptions: { queries: { retry: false } } });
  const onChange = vi.fn();
  render(
    <QueryClientProvider client={queryClient}>
      <RecipientPicker propertyId="property-1" value={value} onChange={onChange} />
    </QueryClientProvider>,
  );
  return { onChange };
}

describe('RecipientPicker', () => {
  beforeEach(() => {
    mockedListRecipientCandidates.mockReset();
    mockedListRecipientCandidates.mockResolvedValue([jean, marie]);
  });

  it('shows no candidate and does not query until the user types something (autocomplete, not a browse-all list)', async () => {
    renderPicker([]);

    // Give any debounce/microtask a chance to run, then assert nothing fired.
    await new Promise((resolve) => setTimeout(resolve, 350));
    expect(mockedListRecipientCandidates).not.toHaveBeenCalled();
    expect(screen.queryByText('Jean Dupont')).not.toBeInTheDocument();
    expect(screen.queryByText('Marie Curie')).not.toBeInTheDocument();
  });

  it('queries and shows candidates once the user starts typing', async () => {
    const user = userEvent.setup();
    renderPicker([]);

    await user.type(screen.getByLabelText('Ajouter un destinataire'), 'Jean');

    await waitFor(() => expect(mockedListRecipientCandidates).toHaveBeenCalled());
    expect(await screen.findByText('Jean Dupont')).toBeInTheDocument();
  });

  it('adds a clicked candidate to the selection instead of navigating', async () => {
    const user = userEvent.setup();
    const { onChange } = renderPicker([]);

    await user.type(screen.getByLabelText('Ajouter un destinataire'), 'Jean');
    await user.click(await screen.findByText('Jean Dupont'));

    expect(onChange).toHaveBeenCalledWith([jean]);
  });

  it('filters already-selected candidates out of the search results', async () => {
    const user = userEvent.setup();
    renderPicker([jean]);

    await user.type(screen.getByLabelText('Ajouter un destinataire'), 'a');

    await waitFor(() => expect(mockedListRecipientCandidates).toHaveBeenCalled());
    expect(await screen.findByText('Marie Curie')).toBeInTheDocument();
    // Jean Dupont should render exactly once - as the chip, not again in the results list.
    expect(screen.getAllByText('Jean Dupont')).toHaveLength(1);
  });

  it('removes a recipient when its chip × button is clicked', async () => {
    const user = userEvent.setup();
    const { onChange } = renderPicker([jean, marie]);

    await user.click(screen.getByRole('button', { name: 'Retirer Jean Dupont' }));

    expect(onChange).toHaveBeenCalledWith([marie]);
  });
});
