import { describe, expect, it, vi } from 'vitest';
import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import type { CurrentUser } from '@/features/identity/me/types/me.types';
import { AVATAR_PRESETS } from '@/features/identity/me/constants/avatarPresets';

const user: CurrentUser = {
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

const mutate = vi.fn();

vi.mock('@/features/identity/me/hooks/useMyAvatarUrl', () => ({
  useMyAvatarUrl: () => ({ url: null, isLoading: false }),
}));
vi.mock('@/features/identity/me/hooks/useUpdateAvatar', () => ({
  useUpdateAvatar: () => ({ mutate, isPending: false, isError: false, error: null }),
}));
vi.mock('@/features/identity/me/hooks/useRemoveAvatar', () => ({
  useRemoveAvatar: () => ({ mutate: vi.fn(), isPending: false, isError: false, error: null }),
}));

const { AvatarUploadForm } = await import('@/features/identity/me/components/AvatarUploadForm');

describe('AvatarUploadForm', () => {
  it('labels the trigger "Changer l\'avatar", not "Changer la photo"', () => {
    render(<AvatarUploadForm user={user} />);

    expect(screen.getByRole('button', { name: /Changer l’avatar/ })).toBeInTheDocument();
    expect(screen.queryByText('Changer la photo')).not.toBeInTheDocument();
  });

  it('keeps the avatar library closed until the trigger is pressed', () => {
    render(<AvatarUploadForm user={user} />);

    expect(screen.queryByRole('dialog')).not.toBeInTheDocument();
  });

  it('opens a dialog offering the whole preset library plus a photo import', async () => {
    render(<AvatarUploadForm user={user} />);
    await userEvent.click(screen.getByRole('button', { name: /Changer l’avatar/ }));

    const dialog = screen.getByRole('dialog');
    expect(dialog).toBeInTheDocument();
    for (const preset of AVATAR_PRESETS) {
      expect(screen.getByRole('button', { name: preset.label })).toBeInTheDocument();
    }
    expect(screen.getByRole('button', { name: 'Importer une photo' })).toBeInTheDocument();
  });

  it('closes the dialog again from its close control', async () => {
    render(<AvatarUploadForm user={user} />);
    await userEvent.click(screen.getByRole('button', { name: /Changer l’avatar/ }));
    await userEvent.click(screen.getAllByRole('button', { name: 'Fermer' })[0]);

    expect(screen.queryByRole('dialog')).not.toBeInTheDocument();
  });
});
