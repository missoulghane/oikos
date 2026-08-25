import { describe, expect, it, vi } from 'vitest';
import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { MemoryRouter } from 'react-router-dom';
import type { Notification } from '@/features/notifications/types/notification.types';

const unread: Notification[] = [
  {
    id: 'n1',
    propertyId: 'p1',
    type: 'GENERAL_MEETING_CALLED',
    title: 'AG convoquée',
    body: 'Assemblée générale le 12 mars.',
    linkPath: '/property-ownership/general-meetings',
    read: false,
    createdAt: '2026-08-20T09:00:00Z',
  },
];

const useMyNotificationsSpy = vi.fn();
const markReadMutate = vi.fn();

vi.mock('@/features/notifications/hooks/useMyNotifications', () => ({
  useMyNotifications: (page: number, size: number, unreadOnly?: boolean) => {
    useMyNotificationsSpy(page, size, unreadOnly);
    return { data: { content: unread, pageNumber: 0, pageSize: size, totalPages: 1, totalElements: unread.length } };
  },
}));
vi.mock('@/features/notifications/hooks/useUnreadNotificationCount', () => ({
  useUnreadNotificationCount: () => ({ data: { unreadCount: 1 } }),
}));
vi.mock('@/features/notifications/hooks/useMarkNotificationRead', () => ({
  useMarkNotificationRead: () => ({ mutate: markReadMutate }),
}));
vi.mock('@/shared/hooks/useEffectiveSpace', () => ({
  useEffectiveSpace: () => ({ kind: 'owner' }),
  spaceQuerySuffix: () => '',
}));

const { NotificationsBell } = await import('@/features/notifications/components/NotificationsBell');

async function openBell() {
  render(
    <MemoryRouter>
      <NotificationsBell />
    </MemoryRouter>,
  );
  await userEvent.click(screen.getByRole('button', { name: 'Notifications' }));
}

describe('NotificationsBell', () => {
  // Filtré côté serveur, pas sur la page reçue : sinon une page de 5 en
  // afficherait parfois 2, et la pastille compterait autre chose que la liste.
  it('asks for the unread ones only', async () => {
    await openBell();

    expect(useMyNotificationsSpy).toHaveBeenCalledWith(0, 5, true);
  });

  it('marks a notification read when it is opened from the bell', async () => {
    await openBell();

    await userEvent.click(screen.getByRole('button', { name: /AG convoquée/ }));

    expect(markReadMutate).toHaveBeenCalledWith('n1');
  });
});
