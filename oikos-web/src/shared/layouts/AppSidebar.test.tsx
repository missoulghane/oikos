import { describe, expect, it, vi } from 'vitest';
import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { MemoryRouter } from 'react-router-dom';
import type { CurrentUser } from '@/features/identity/me/types/me.types';

const owner: CurrentUser = {
  id: 'user-2',
  fullName: 'Salma Idrissi',
  email: 'user2@oikos.com',
  phone: null,
  roles: [],
  roleByProperty: {},
  verified: true,
  enabled: true,
  hasAvatar: false,
};

vi.mock('@/features/identity/me', () => ({
  useCurrentUser: () => ({ data: owner }),
  boardPropertyIds: () => [],
  canManageProperties: () => false,
  isManagerTier: () => false,
}));
vi.mock('@/features/messaging', () => ({
  useUnreadSummary: () => ({ data: { totalUnreadMessageCount: 0 } }),
}));
vi.mock('@/shared/hooks/useEffectiveSpace', () => ({
  useEffectiveSpace: () => ({ kind: 'owner' }),
  spaceQuerySuffix: () => '',
}));
const closeMobileSidebar = vi.fn();

vi.mock('@/shared/context/SidebarContext', () => ({
  useSidebar: () => ({
    isExpanded: true,
    isMobileOpen: true,
    isHovered: false,
    toggleSidebar: vi.fn(),
    toggleMobileSidebar: vi.fn(),
    closeMobileSidebar,
    setIsHovered: vi.fn(),
  }),
}));

const { AppSidebar } = await import('@/shared/layouts/AppSidebar');

function renderSidebar() {
  return render(
    <MemoryRouter>
      <AppSidebar />
    </MemoryRouter>,
  );
}

describe('AppSidebar (owner space)', () => {
  it('still lists the owner self-service entries', () => {
    renderSidebar();

    expect(screen.getByText('Mon tableau de bord')).toBeInTheDocument();
    expect(screen.getByText('Mes échéances')).toBeInTheDocument();
    expect(screen.getByText('Mes paiements')).toBeInTheDocument();
  });

  // The dashboard IS the lot list now, so a "Mes lots" entry would duplicate it.
  it('does not show a separate "Mes lots" entry', () => {
    renderSidebar();

    expect(screen.queryByText('Mes lots')).not.toBeInTheDocument();
  });

  it('does not show a "Mes invitations" entry', () => {
    renderSidebar();

    expect(screen.queryByText('Mes invitations')).not.toBeInTheDocument();
  });

  it('does not show a "Notifications" entry', () => {
    renderSidebar();

    expect(screen.queryByText('Notifications')).not.toBeInTheDocument();
  });

  // The drawer covers most of the screen on mobile, so leaving it open after a
  // tap hides the very page the user asked for.
  it('closes the mobile drawer when a nav entry is tapped', async () => {
    renderSidebar();
    closeMobileSidebar.mockClear();

    await userEvent.click(screen.getByText('Mon tableau de bord'));

    expect(closeMobileSidebar).toHaveBeenCalled();
  });

  it('closes it too when tapping the entry of the page already open', async () => {
    render(
      <MemoryRouter initialEntries={['/dashboard']}>
        <AppSidebar />
      </MemoryRouter>,
    );
    closeMobileSidebar.mockClear();

    // Same route: no navigation happens, so only the link's own handler can close it
    await userEvent.click(screen.getByText('Mon tableau de bord'));

    expect(closeMobileSidebar).toHaveBeenCalled();
  });
});
