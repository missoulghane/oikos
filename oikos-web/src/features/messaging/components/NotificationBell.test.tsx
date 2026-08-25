import { describe, expect, it, vi } from 'vitest';
import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { MemoryRouter } from 'react-router-dom';

const unreadSummary = {
  totalUnreadMessageCount: 2,
  recentUnread: [
    {
      id: 'conv-1',
      type: 'GROUP' as const,
      propertyId: 'p1',
      propertyName: 'Al Amal',
      subject: 'Fuite au 3e',
      concernsUnit: null,
      participants: [],
      // Ce que produit l'éditeur : du HTML, affiché tel quel jusqu'ici.
      lastMessagePreview: '<p>Bonjour à <strong>tous</strong></p><p>La fuite est réparée.</p>',
      lastMessageAt: '2026-08-20T09:00:00Z',
      unreadCount: 2,
      messageCount: 5,
    },
  ],
};

vi.mock('@/features/messaging/hooks/useUnreadSummary', () => ({
  useUnreadSummary: () => ({ data: unreadSummary, isLoading: false, isError: false, error: null }),
}));
vi.mock('@/shared/hooks/useEffectiveSpace', () => ({
  useEffectiveSpace: () => ({ kind: 'owner' }),
  spaceQuerySuffix: () => '',
}));

const { NotificationBell } = await import('@/features/messaging/components/NotificationBell');

async function openBell() {
  render(
    <MemoryRouter>
      <NotificationBell />
    </MemoryRouter>,
  );
  await userEvent.click(screen.getByRole('button', { name: 'Notifications de messagerie' }));
}

describe('NotificationBell (messagerie)', () => {
  it('shows the preview as plain text, never the markup it is written in', async () => {
    await openBell();

    expect(screen.getByText('Bonjour à tous La fuite est réparée.')).toBeInTheDocument();
    expect(screen.queryByText(/<p>|<strong>/)).not.toBeInTheDocument();
  });

  it('opens the conversation the preview belongs to', async () => {
    await openBell();

    expect(screen.getByRole('link', { name: /Fuite au 3e/ })).toHaveAttribute(
      'href',
      '/messages/reception/conv-1',
    );
  });
});
