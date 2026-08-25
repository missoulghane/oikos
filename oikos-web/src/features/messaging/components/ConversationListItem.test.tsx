import { describe, expect, it } from 'vitest';
import { render, screen } from '@testing-library/react';
import { MemoryRouter } from 'react-router-dom';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { ConversationListItem } from '@/features/messaging/components/ConversationListItem';
import type { ConversationSummary } from '@/features/messaging/types/messaging.types';

const baseConversation: ConversationSummary = {
  id: 'conversation-1',
  type: 'GROUP',
  propertyId: 'property-1',
  propertyName: 'Résidence Les Oliviers',
  subject: 'Fuite d’eau hall B',
  concernsUnit: null,
  participants: [{ userId: 'user-2', fullName: 'Jean Dupont' }],
  lastMessagePreview: 'À bientôt !',
  lastMessageAt: new Date().toISOString(),
  unreadCount: 0,
  messageCount: 1,
};

// La ligne porte désormais le bouton « Lu / Non lu », donc une mutation, donc
// un QueryClient.
function renderItem(conversation: ConversationSummary, box: 'RECEIVED' | 'SENT' = 'RECEIVED') {
  const queryClient = new QueryClient({ defaultOptions: { queries: { retry: false } } });
  return render(
    <QueryClientProvider client={queryClient}>
      <MemoryRouter>
        <ul>
          <ConversationListItem conversation={conversation} box={box} />
        </ul>
      </MemoryRouter>
    </QueryClientProvider>,
  );
}

describe('ConversationListItem', () => {
  it('renders the sender name in the header line, and the subject/property line below', () => {
    renderItem(baseConversation);

    expect(screen.getByText('Jean Dupont')).toBeInTheDocument();
    expect(screen.getByText('Fuite d’eau hall B · Résidence Les Oliviers')).toBeInTheDocument();
  });

  it('renders a preview of the last message below the subject line', () => {
    renderItem(baseConversation);

    expect(screen.getByText('À bientôt !')).toBeInTheDocument();
  });

  it('truncates a preview longer than 100 characters', () => {
    const longPreview = 'a'.repeat(150);
    renderItem({ ...baseConversation, lastMessagePreview: longPreview });

    expect(screen.getByText(`${'a'.repeat(100)}…`)).toBeInTheDocument();
  });

  it('joins every participant name into the sender line for a multi-recipient GROUP conversation', () => {
    renderItem({
      ...baseConversation,
      participants: [
        { userId: 'user-2', fullName: 'Jean Dupont' },
        { userId: 'user-3', fullName: 'Marie Curie' },
      ],
    });

    expect(screen.getByText('Jean Dupont, Marie Curie')).toBeInTheDocument();
  });

  it('renders a distinct label for a BROADCAST conversation, without a participant line', () => {
    renderItem({
      ...baseConversation,
      type: 'BROADCAST',
      subject: null,
      participants: [],
    });

    expect(screen.getByText('Annonces de la copropriété')).toBeInTheDocument();
    expect(screen.queryByText('Jean Dupont')).not.toBeInTheDocument();
    expect(screen.getByText('Résidence Les Oliviers')).toBeInTheDocument();
  });

  it('shows a "Privé · conseil" chip for a BOARD_PRIVATE conversation, keeping its own subject as the title', () => {
    renderItem({
      ...baseConversation,
      type: 'BOARD_PRIVATE',
      subject: 'Devis ascenseur',
      participants: [],
    });

    expect(screen.getByText('Devis ascenseur')).toBeInTheDocument();
    expect(screen.getByText('Privé · conseil')).toBeInTheDocument();
  });

  it('shows a "Concerne" chip when the conversation has a concernsUnit', () => {
    renderItem({ ...baseConversation, concernsUnit: 'Appartement 3' });

    expect(screen.getByText('Concerne Appartement 3')).toBeInTheDocument();
  });

  it('does not show a "Concerne" chip when concernsUnit is null', () => {
    renderItem(baseConversation);

    expect(screen.queryByText(/^Concerne /)).not.toBeInTheDocument();
  });

  it('shows an unread badge and darker sender styling when unreadCount > 0', () => {
    renderItem({ ...baseConversation, unreadCount: 3 });

    expect(screen.getByText('3')).toBeInTheDocument();
    expect(screen.getByText('Jean Dupont')).toHaveClass('text-gray-900');
  });

  it('does not show an unread badge when the conversation is fully read', () => {
    renderItem(baseConversation);

    expect(screen.queryByText('0')).not.toBeInTheDocument();
    expect(screen.getByText('Jean Dupont')).toHaveClass('text-gray-700');
  });

  it('does not show a message count for a plain single message', () => {
    renderItem(baseConversation);

    expect(screen.queryByText(/messages\)/)).not.toBeInTheDocument();
  });

  it('shows a message count once the conversation has replies', () => {
    renderItem({ ...baseConversation, messageCount: 4 });

    expect(screen.getByText('(4 messages)')).toBeInTheDocument();
  });

  // Marquer lu/non lu se fait depuis la liste, sans ouvrir la conversation :
  // le bouton est donc hors du lien, et il nomme le geste, pas l'état.
  it('offers to mark an unread conversation as read, and a read one as unread', () => {
    renderItem({ ...baseConversation, unreadCount: 2 });

    expect(screen.getByRole('button', { name: /Marquer .* comme lu/ })).toBeInTheDocument();
  });

  it('offers the opposite once nothing is unread', () => {
    renderItem(baseConversation);

    expect(screen.getByRole('button', { name: /Marquer .* comme non lu/ })).toBeInTheDocument();
  });

  it('links to the conversation under whichever box (Réception/Envoyé) it was opened from', () => {
    renderItem(baseConversation, 'RECEIVED');
    expect(screen.getByRole('link')).toHaveAttribute('href', '/messages/reception/conversation-1');

    renderItem(baseConversation, 'SENT');
    expect(screen.getAllByRole('link')[1]).toHaveAttribute('href', '/messages/sent/conversation-1');
  });
});
