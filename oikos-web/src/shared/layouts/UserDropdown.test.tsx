import { describe, expect, it, vi, beforeEach } from 'vitest';
import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { MemoryRouter } from 'react-router-dom';
import type { CurrentUser } from '@/features/identity/me/types/me.types';

const currentUser: CurrentUser = {
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
  useCurrentUser: () => ({ data: currentUser }),
}));
vi.mock('@/features/identity/me/hooks/useMyAvatarUrl', () => ({
  useMyAvatarUrl: () => ({ url: null, isLoading: false }),
}));
vi.mock('@/shared/hooks/useEffectiveSpace', () => ({
  useEffectiveSpace: () => ({ kind: 'owner' }),
  spaceQuerySuffix: () => '',
}));
vi.mock('@/app/store', () => ({
  useAuthStore: (selector: (state: { clearSession: () => void }) => unknown) =>
    selector({ clearSession: vi.fn() }),
}));

const { UserDropdown } = await import('@/shared/layouts/UserDropdown');

function renderDropdown() {
  return render(
    <MemoryRouter>
      <UserDropdown />
    </MemoryRouter>,
  );
}

describe('UserDropdown', () => {
  beforeEach(() => {
    // 10:00 -> "Bonjour"; the evening case is covered in getGreeting.test.ts
    vi.setSystemTime(new Date(2026, 0, 1, 10));
  });

  it('shows only the first name next to the avatar, never the full name', () => {
    renderDropdown();

    expect(screen.getByText('Salma')).toBeInTheDocument();
    expect(screen.queryByText('Salma Idrissi')).not.toBeInTheDocument();
  });

  it('greets the user by first name and does not show their email once opened', async () => {
    renderDropdown();
    await userEvent.click(screen.getByRole('button'));

    expect(screen.getByText('Bonjour Salma')).toBeInTheDocument();
    expect(screen.queryByText('user2@oikos.com')).not.toBeInTheDocument();
  });

  it('links to the profile under the "Mon profil" label', async () => {
    renderDropdown();
    await userEvent.click(screen.getByRole('button'));

    expect(screen.getByRole('link', { name: /Mon profil/ })).toHaveAttribute('href', '/profile');
    expect(screen.queryByText('Mes informations')).not.toBeInTheDocument();
  });
});
