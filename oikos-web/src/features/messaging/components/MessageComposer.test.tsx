import type { ComponentProps } from 'react';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import { render, screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { MessageComposer } from '@/features/messaging/components/MessageComposer';
import { sendMessage } from '@/features/messaging/api/sendMessage';
import type { Message } from '@/features/messaging/types/messaging.types';

// Factory form (not bare automock): a bare `vi.mock(path)` still loads the
// real module to derive its shape, which would drag in httpClient -> env.ts
// (VITE_API_URL) with no .env available under Vitest.
vi.mock('@/features/messaging/api/sendMessage', () => ({
  sendMessage: vi.fn(),
}));

const mockedSendMessage = vi.mocked(sendMessage);

const sentMessage: Message = {
  id: 'message-1',
  conversationId: 'conversation-1',
  senderId: 'user-1',
  senderName: 'Jean Dupont',
  senderIdentity: 'OWNER',
  body: 'Bonjour',
  createdAt: new Date().toISOString(),
  mine: true,
};

function renderComposer(props: Partial<ComponentProps<typeof MessageComposer>> = {}) {
  const queryClient = new QueryClient({ defaultOptions: { mutations: { retry: false } } });
  return render(
    <QueryClientProvider client={queryClient}>
      <MessageComposer conversationId="conversation-1" {...props} />
    </QueryClientProvider>,
  );
}

describe('MessageComposer', () => {
  beforeEach(() => {
    mockedSendMessage.mockReset();
  });

  it('shows a validation error and blocks submission when the message is empty', async () => {
    const user = userEvent.setup();
    renderComposer();

    await user.click(screen.getByRole('button', { name: /envoyer/i }));

    expect(await screen.findByText('Le message ne peut pas être vide')).toBeInTheDocument();
    expect(mockedSendMessage).not.toHaveBeenCalled();
  });

  it('sends the message and clears the draft on success', async () => {
    mockedSendMessage.mockResolvedValueOnce(sentMessage);
    const user = userEvent.setup();
    renderComposer();

    const textarea = screen.getByLabelText('Message');
    await user.type(textarea, 'Bonjour');
    await user.click(screen.getByRole('button', { name: /envoyer/i }));

    await waitFor(() => expect(mockedSendMessage).toHaveBeenCalledWith('conversation-1', { body: 'Bonjour' }));
    await waitFor(() => expect(textarea).toHaveValue(''));
  });

  it('shows an error with a retry button that resubmits the same draft', async () => {
    mockedSendMessage.mockRejectedValueOnce({
      isAxiosError: true,
      response: { data: { message: 'Erreur serveur' } },
    });
    const user = userEvent.setup();
    renderComposer();

    const textarea = screen.getByLabelText('Message');
    await user.type(textarea, 'Bonjour');
    await user.click(screen.getByRole('button', { name: /envoyer/i }));

    expect(await screen.findByText('Erreur serveur')).toBeInTheDocument();
    expect(textarea).toHaveValue('Bonjour');

    mockedSendMessage.mockResolvedValueOnce(sentMessage);
    await user.click(screen.getByRole('button', { name: /réessayer/i }));

    await waitFor(() => expect(mockedSendMessage).toHaveBeenCalledTimes(2));
    expect(mockedSendMessage).toHaveBeenNthCalledWith(2, 'conversation-1', { body: 'Bonjour' });
    await waitFor(() => expect(textarea).toHaveValue(''));
  });

  it('does not show the "répondre en tant que" picker when the sender only holds one role here', () => {
    renderComposer();

    expect(screen.queryByText('Répondre en tant que')).not.toBeInTheDocument();
  });

  it('shows the picker preselected on the active space, and sends the chosen identity', async () => {
    mockedSendMessage.mockResolvedValueOnce(sentMessage);
    const user = userEvent.setup();
    renderComposer({ identityChoiceNeeded: true, defaultIdentity: 'BOARD' });

    expect(screen.getByRole('button', { name: 'Membre du bureau' })).toHaveAttribute('aria-pressed', 'true');
    expect(screen.getByRole('button', { name: 'Copropriétaire' })).toHaveAttribute('aria-pressed', 'false');

    await user.click(screen.getByRole('button', { name: 'Copropriétaire' }));
    await user.type(screen.getByLabelText('Message'), 'Bonjour');
    await user.click(screen.getByRole('button', { name: /envoyer/i }));

    await waitFor(() =>
      expect(mockedSendMessage).toHaveBeenCalledWith('conversation-1', { body: 'Bonjour', senderIdentity: 'OWNER' }),
    );
  });
});
