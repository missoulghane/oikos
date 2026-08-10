import { describe, expect, it } from 'vitest';
import { render, screen } from '@testing-library/react';
import { MemoryRouter } from 'react-router-dom';
import { ConversationListItem } from '@/features/messaging/components/ConversationListItem';
import type { ConversationSummary } from '@/features/messaging/types/messaging.types';

const baseConversation: ConversationSummary = {
  id: 'conversation-1',
  type: 'GROUP',
  propertyId: 'property-1',
  propertyName: 'Résidence Les Oliviers',
  subject: 'Fuite d’eau hall B',
  participants: [{ userId: 'user-2', fullName: 'Jean Dupont' }],
  lastMessagePreview: 'À bientôt !',
  lastMessageAt: new Date().toISOString(),
  unreadCount: 0,
  messageCount: 1,
};

function renderItem(conversation: ConversationSummary) {
  return render(
    <MemoryRouter>
      <ul>
        <ConversationListItem conversation={conversation} />
      </ul>
    </MemoryRouter>,
  );
}

describe('ConversationListItem', () => {
  it('renders the subject as the title, and the participant/property line below', () => {
    renderItem(baseConversation);

    expect(screen.getByText('Fuite d’eau hall B')).toBeInTheDocument();
    expect(screen.getByText('À bientôt !')).toBeInTheDocument();
    expect(screen.getByText('Jean Dupont · Résidence Les Oliviers')).toBeInTheDocument();
  });

  it('joins every participant name on the secondary line for a multi-recipient GROUP conversation', () => {
    renderItem({
      ...baseConversation,
      participants: [
        { userId: 'user-2', fullName: 'Jean Dupont' },
        { userId: 'user-3', fullName: 'Marie Curie' },
      ],
    });

    expect(screen.getByText('Jean Dupont, Marie Curie · Résidence Les Oliviers')).toBeInTheDocument();
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

  it('shows an unread badge and bold styling when unreadCount > 0', () => {
    renderItem({ ...baseConversation, unreadCount: 3 });

    expect(screen.getByText('3')).toBeInTheDocument();
    expect(screen.getByText('Fuite d’eau hall B')).toHaveClass('font-semibold');
  });

  it('does not show an unread badge when the conversation is fully read', () => {
    renderItem(baseConversation);

    expect(screen.queryByText('0')).not.toBeInTheDocument();
    expect(screen.getByText('Fuite d’eau hall B')).toHaveClass('font-medium');
  });

  it('does not show a message count for a plain single message', () => {
    renderItem(baseConversation);

    expect(screen.queryByText(/messages\)/)).not.toBeInTheDocument();
  });

  it('shows a message count once the conversation has replies', () => {
    renderItem({ ...baseConversation, messageCount: 4 });

    expect(screen.getByText('(4 messages)')).toBeInTheDocument();
  });
});
