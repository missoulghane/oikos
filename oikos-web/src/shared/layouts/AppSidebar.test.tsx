import { describe, expect, it, vi } from 'vitest';
import { render, screen } from '@testing-library/react';
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

    expect(screen.getByText('Mes lots')).toBeInTheDocument();
    expect(screen.getByText('Mes échéances')).toBeInTheDocument();
    expect(screen.getByText('Mes paiements')).toBeInTheDocument();
  });

  it('does not show a "Mes invitations" entry', () => {
    renderSidebar();

    expect(screen.queryByText('Mes invitations')).not.toBeInTheDocument();
  });

  it('does not show a "Notifications" entry', () => {
    renderSidebar();

    expect(screen.queryByText('Notifications')).not.toBeInTheDocument();
  });
});
