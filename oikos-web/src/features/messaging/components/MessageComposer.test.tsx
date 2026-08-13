import { StrictMode, type ComponentProps } from 'react';
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

// The body is Quill's HTML output (see MessageBodyEditor/QuillEditor), and
// exactly how it wraps a plain word in <p> tags is a jsdom/userEvent
// contenteditable-typing quirk, not part of the app's actual contract - these
// tests only assert the visible text made it through intact, not the exact
// markup.
function plainText(html: string): string {
  return html.replace(/<[^>]*>/g, '').trim();
}

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

    const editor = screen.getByLabelText('Message');
    await user.type(editor, 'Bonjour');
    await user.click(screen.getByRole('button', { name: /envoyer/i }));

    await waitFor(() => expect(mockedSendMessage).toHaveBeenCalledTimes(1));
    const [conversationId, payload] = mockedSendMessage.mock.calls[0];
    expect(conversationId).toBe('conversation-1');
    expect(plainText((payload as { body: string }).body)).toBe('Bonjour');
    await waitFor(() => expect(editor).toHaveTextContent(''));
  });

  it('shows an error with a retry button that resubmits the same draft', async () => {
    mockedSendMessage.mockRejectedValueOnce({
      isAxiosError: true,
      response: { data: { message: 'Erreur serveur' } },
    });
    const user = userEvent.setup();
    renderComposer();

    const editor = screen.getByLabelText('Message');
    await user.type(editor, 'Bonjour');
    await user.click(screen.getByRole('button', { name: /envoyer/i }));

    expect(await screen.findByText('Erreur serveur')).toBeInTheDocument();
    expect(editor).toHaveTextContent('Bonjour');

    mockedSendMessage.mockResolvedValueOnce(sentMessage);
    await user.click(screen.getByRole('button', { name: /réessayer/i }));

    await waitFor(() => expect(mockedSendMessage).toHaveBeenCalledTimes(2));
    const [, secondPayload] = mockedSendMessage.mock.calls[1];
    expect(plainText((secondPayload as { body: string }).body)).toBe('Bonjour');
    await waitFor(() => expect(editor).toHaveTextContent(''));
  });

  it('mounts exactly one Quill toolbar/editor under StrictMode', () => {
    // StrictMode double-invokes mount effects in dev to surface missing
    // cleanup - QuillEditor's mount effect used to have none, so Quill's own
    // DOM insertions (the toolbar, the editor root) piled up a second time.
    const queryClient = new QueryClient({ defaultOptions: { mutations: { retry: false } } });
    const { container } = render(
      <StrictMode>
        <QueryClientProvider client={queryClient}>
          <MessageComposer conversationId="conversation-1" />
        </QueryClientProvider>
      </StrictMode>,
    );

    expect(container.querySelectorAll('.ql-toolbar')).toHaveLength(1);
    expect(container.querySelectorAll('.ql-editor')).toHaveLength(1);
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

    await waitFor(() => expect(mockedSendMessage).toHaveBeenCalledTimes(1));
    const [conversationId, payload] = mockedSendMessage.mock.calls[0];
    expect(conversationId).toBe('conversation-1');
    expect(plainText((payload as { body: string }).body)).toBe('Bonjour');
    expect((payload as { senderIdentity?: string }).senderIdentity).toBe('OWNER');
  });
});
