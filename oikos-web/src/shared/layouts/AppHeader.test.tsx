import { describe, expect, it, vi } from 'vitest';
import { render, screen } from '@testing-library/react';
import { MemoryRouter } from 'react-router-dom';

vi.mock('@/shared/context/SidebarContext', () => ({
  useSidebar: () => ({
    isExpanded: true,
    isMobileOpen: false,
    isHovered: false,
    toggleSidebar: vi.fn(),
    toggleMobileSidebar: vi.fn(),
    setIsHovered: vi.fn(),
  }),
}));
vi.mock('@/shared/layouts/UserDropdown', () => ({ UserDropdown: () => <div /> }));
vi.mock('@/shared/components/ThemeToggleButton/ThemeToggleButton', () => ({ ThemeToggleButton: () => <div /> }));
vi.mock('@/shared/components/SpaceSwitcher/SpaceSwitcher', () => ({ SpaceSwitcher: () => <div /> }));
vi.mock('@/features/messaging', () => ({
  NotificationBell: () => <button type="button" aria-label="Notifications de messagerie" />,
}));
vi.mock('@/features/notifications', () => ({
  NotificationsBell: () => <button type="button" aria-label="Notifications" />,
}));

const { AppHeader } = await import('@/shared/layouts/AppHeader');

describe('AppHeader', () => {
  // The sidebar "Notifications" entry was removed on purpose; this bell is now
  // the only way into /notifications, so it must never be dropped as dead code.
  it('keeps the notifications bell as the entry point to the notification centre', () => {
    render(
      <MemoryRouter>
        <AppHeader />
      </MemoryRouter>,
    );

    expect(screen.getByRole('button', { name: 'Notifications' })).toBeInTheDocument();
  });

  it('keeps the messaging bell alongside it', () => {
    render(
      <MemoryRouter>
        <AppHeader />
      </MemoryRouter>,
    );

    expect(screen.getByRole('button', { name: 'Notifications de messagerie' })).toBeInTheDocument();
  });
});
