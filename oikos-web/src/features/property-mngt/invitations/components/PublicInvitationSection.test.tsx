import { beforeEach, describe, expect, it, vi } from 'vitest';
import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import type { Invitation } from '@/features/property-mngt/invitations/types/invitation.types';

const createMutate = vi.fn();
const disableMutate = vi.fn();
const enableMutate = vi.fn();
let invitation: Invitation | null = null;

vi.mock('@/features/property-mngt/invitations/hooks/usePublicInvitation', () => ({
  usePublicInvitation: () => ({ data: { invitation }, isLoading: false, isError: false }),
}));
vi.mock('@/features/property-mngt/invitations/hooks/useCreateInvitation', () => ({
  useCreateInvitation: () => ({ mutate: createMutate, isPending: false, isError: false }),
}));
vi.mock('@/features/property-mngt/invitations/hooks/useDisableInvitation', () => ({
  useDisableInvitation: () => ({ mutate: disableMutate, isPending: false, isError: false }),
}));
vi.mock('@/features/property-mngt/invitations/hooks/useEnableInvitation', () => ({
  useEnableInvitation: () => ({ mutate: enableMutate, isPending: false, isError: false }),
}));

const { PublicInvitationSection } = await import(
  '@/features/property-mngt/invitations/components/PublicInvitationSection'
);

function publicLink(override: Partial<Invitation> = {}): Invitation {
  return {
    id: 'inv-1',
    propertyId: 'p-1',
    type: 'PUBLIC',
    targetRole: 'PROPERTY_OWNER',
    boardRole: null,
    targetEmail: null,
    link: 'https://app.example.com/invitations?token=tok',
    status: 'ACTIVE',
    expiresAt: '2026-12-31T00:00:00Z',
    createdByUserId: 'user-1',
    emailMismatch: false,
    ...override,
  };
}

describe('PublicInvitationSection', () => {
  beforeEach(() => {
    invitation = null;
    createMutate.mockClear();
    disableMutate.mockClear();
    enableMutate.mockClear();
  });

  it("propose de créer le lien tant que la copropriété n'en a pas", async () => {
    render(<PublicInvitationSection propertyId="p-1" />);

    await userEvent.click(screen.getByRole('button', { name: 'Créer le lien public' }));

    expect(createMutate).toHaveBeenCalledWith({ propertyId: 'p-1', type: 'PUBLIC' });
  });

  // Un second lien invaliderait silencieusement le premier - celui dont le QR
  // code est imprimé dans le hall.
  it('ne propose plus de créer un lien une fois qu\'il en existe un', () => {
    invitation = publicLink();
    render(<PublicInvitationSection propertyId="p-1" />);

    expect(screen.queryByRole('button', { name: 'Créer le lien public' })).not.toBeInTheDocument();
    expect(screen.getByText('https://app.example.com/invitations?token=tok')).toBeInTheDocument();
    expect(screen.getByText('Actif')).toBeInTheDocument();
  });

  it('ferme les adhésions en désactivant le lien', async () => {
    invitation = publicLink();
    render(<PublicInvitationSection propertyId="p-1" />);

    await userEvent.click(screen.getByRole('button', { name: 'Désactiver' }));

    expect(disableMutate).toHaveBeenCalledWith('inv-1');
  });

  // Rouvert, jamais recréé : le lien désactivé reste affiché et réactivable.
  it('rouvre les adhésions en réactivant le même lien', async () => {
    invitation = publicLink({ status: 'DISABLED' });
    render(<PublicInvitationSection propertyId="p-1" />);

    expect(screen.getByText('Désactivé')).toBeInTheDocument();
    expect(screen.queryByRole('button', { name: 'Créer le lien public' })).not.toBeInTheDocument();

    await userEvent.click(screen.getByRole('button', { name: 'Réactiver' }));

    expect(enableMutate).toHaveBeenCalledWith('inv-1');
  });
});
