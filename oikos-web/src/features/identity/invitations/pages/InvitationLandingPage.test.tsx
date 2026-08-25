import { beforeEach, describe, expect, it, vi } from 'vitest';
import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { MemoryRouter, Route, Routes } from 'react-router-dom';
import { ThemeProvider } from '@/shared/context/ThemeContext';
import type { InvitationPreview, InvitationType } from '@/features/identity/invitations/types/invitation.types';

// Mutable: the very same link, sent either way. The factories below only read
// it at render time, well after this module has initialised.
let previewType: InvitationType = 'PUBLIC';
// Le lot désigné par une invitation privée. Nul = lien public, ou lot supprimé
// depuis l'envoi.
let targetUnit: { id: string; number: string; typeName: string } | null = null;
let boardRole: string | null = null;

function preview(): InvitationPreview {
  return {
    type: previewType,
    usable: true,
    reason: null,
    propertyName: 'Résidence Al Amal',
    propertyAddress: '12 rue des Orangers, Casablanca',
    targetEmail: null,
    boardRole,
    targetUnitId: targetUnit?.id ?? null,
    targetUnitNumber: targetUnit?.number ?? null,
    targetUnitTypeName: targetUnit?.typeName ?? null,
  };
}

vi.mock('@/app/store', () => ({
  useAuthStore: (selector: (state: { isAuthenticated: boolean }) => unknown) =>
    selector({ isAuthenticated: false }),
}));
vi.mock('@/features/identity/invitations/hooks/useInvitationPreview', () => ({
  useInvitationPreview: () => ({ data: preview(), isPending: false, isError: false }),
}));
vi.mock('@/features/identity/invitations/hooks/useInvitationAvailableUnits', () => ({
  useInvitationAvailableUnits: () => ({
    data: { content: [{ id: 'unit-1', unitNumber: 'A-12', unitTypeName: 'Appartement' }] },
  }),
}));
vi.mock('@/features/identity/invitations/hooks/useAcceptInvitation', () => ({
  useAcceptInvitation: () => ({ mutateAsync: vi.fn() }),
}));
vi.mock('@/features/identity/invitations/hooks/useSubmitMembershipRequest', () => ({
  useSubmitMembershipRequest: () => ({ mutateAsync: vi.fn() }),
}));

const { InvitationLandingPage } = await import(
  '@/features/identity/invitations/pages/InvitationLandingPage'
);

// AuthLayout porte le bouton clair/sombre, qui exige le ThemeProvider : c'est
// la page entière qui est testée ici, layout compris, pas le seul contenu.
function renderPage(search = '?token=tok-1') {
  return render(
    <ThemeProvider>
      <MemoryRouter initialEntries={[`/invitations${search}`]}>
        <Routes>
          <Route path="/invitations" element={<InvitationLandingPage />} />
        </Routes>
      </MemoryRouter>
    </ThemeProvider>,
  );
}

/**
 * Le visiteur arrive d'un lien reçu par message ou par email. La page s'ouvrait
 * sur le nom de la copropriété et un sélecteur de lot, sans jamais dire où il
 * était ni ce qu'il s'apprêtait à faire.
 */
describe('InvitationLandingPage', () => {
  beforeEach(() => {
    previewType = 'PUBLIC';
    targetUnit = null;
    boardRole = null;
  });

  it("annonce l'espace que le lien fait rejoindre, et la copropriété", () => {
    renderPage();

    expect(screen.getByRole('heading', { name: 'Rejoignez votre espace copropriétaire' })).toBeInTheDocument();
    expect(screen.getByText('Résidence Al Amal')).toBeInTheDocument();
  });

  // Deux issues différentes : un lien public ne donne pas l'accès, il dépose
  // une demande. Promettre la même chose aux deux, c'est promettre au second
  // un espace qu'il ne verra pas ce jour-là.
  it('annonce la validation du syndic sur un lien public', () => {
    renderPage();

    expect(screen.getByText(/Le syndic prend le relais/)).toBeInTheDocument();
    expect(screen.getByText(/vous recevrez une notification/)).toBeInTheDocument();
  });

  // Un lien privé dépose la même demande qu'un lien public : c'est ce qui rend
  // sans danger le changement de lot ci-dessous.
  it('annonce la même validation du syndic sur un lien privé', () => {
    previewType = 'PRIVATE';
    targetUnit = { id: 'unit-1', number: 'A-12', typeName: 'Appartement' };
    renderPage();

    expect(screen.getByText(/Le syndic prend le relais/)).toBeInTheDocument();
  });

  // « Votre lot », sans dire lequel, ne se vérifie pas : c'est la première
  // chose que le destinataire doit pouvoir contrôler.
  it('annonce le lot désigné plutôt que de le demander', () => {
    previewType = 'PRIVATE';
    targetUnit = { id: 'unit-1', number: 'A-12', typeName: 'Appartement' };
    renderPage();

    expect(screen.getByText('Votre lot')).toBeInTheDocument();
    expect(screen.getByText('A-12 — Appartement')).toBeInTheDocument();
    expect(screen.getByText('1. Confirmez votre lot')).toBeInTheDocument();
    expect(screen.queryByLabelText('Lot')).not.toBeInTheDocument();
  });

  // Un syndic se trompe de ligne, et le lot désigné n'engage personne tant que
  // la demande n'est pas validée.
  it("ouvre le sélecteur quand l'invité récuse le lot désigné", async () => {
    previewType = 'PRIVATE';
    targetUnit = { id: 'unit-1', number: 'A-12', typeName: 'Appartement' };
    renderPage();

    await userEvent.click(screen.getByRole('checkbox', { name: /Ce n'est pas votre lot/ }));

    // Le bloc « Votre lot » disparaît ; le même lot reste choisissable dans la
    // liste, comme n'importe quel autre.
    expect(screen.queryByText('Votre lot')).not.toBeInTheDocument();
    expect(screen.getByText('1. Choisissez votre lot')).toBeInTheDocument();
    expect(screen.getByLabelText('Lot')).toBeInTheDocument();
  });

  // Le lot désigné vaut désignation : la certification s'affiche sans que
  // l'invité ait à re-sélectionner ce qu'on lui propose déjà.
  it('demande la certification dès le lot désigné, sans re-sélection', () => {
    previewType = 'PRIVATE';
    targetUnit = { id: 'unit-1', number: 'A-12', typeName: 'Appartement' };
    renderPage();

    expect(
      screen.getByRole('checkbox', { name: /Je certifie être le propriétaire ou le mandataire pour ce lot/ }),
    ).toBeInTheDocument();
    expect(screen.queryByText(/2\. Connectez-vous ou créez un compte/)).not.toBeInTheDocument();
  });

  // Un siège au conseil syndical ne porte sur aucun lot : ni sélecteur, ni
  // certification, ni promesse de validation d'une demande d'adhésion.
  it('ne demande ni lot ni certification pour un siège au conseil syndical', () => {
    previewType = 'PRIVATE';
    boardRole = 'PRESIDENT';
    renderPage();

    expect(screen.queryByRole('checkbox')).not.toBeInTheDocument();
    expect(screen.queryByText(/Le syndic prend le relais/)).not.toBeInTheDocument();
    expect(screen.getByText('Connectez-vous ou créez un compte')).toBeInTheDocument();
  });

  // Un lien public circule librement : n'importe qui peut désigner n'importe
  // quel lot libre. L'étape suivante ne s'ouvre qu'une fois la désignation
  // assumée par son auteur.
  it("n'ouvre l'étape 2 qu'une fois la propriété du lot certifiée", async () => {
    renderPage('?token=tok-1&unitId=unit-1');

    expect(screen.queryByText(/Connectez-vous ou créez un compte/)).not.toBeInTheDocument();

    await userEvent.click(
      screen.getByRole('checkbox', { name: /Je certifie être le propriétaire ou le mandataire pour ce lot/ }),
    );

    expect(screen.getByText(/Connectez-vous ou créez un compte/)).toBeInTheDocument();
  });

  // La case porte sur « ce lot » : elle n'a rien à dire d'un autre.
});
