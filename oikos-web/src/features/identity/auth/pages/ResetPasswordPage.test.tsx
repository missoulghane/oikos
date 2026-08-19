import { beforeEach, describe, expect, it, vi } from 'vitest';
import { render, screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { MemoryRouter } from 'react-router-dom';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { ThemeProvider } from '@/shared/context/ThemeContext';
import { ResetPasswordPage } from '@/features/identity/auth/pages/ResetPasswordPage';
import { resetPassword } from '@/features/identity/auth/api/resetPassword';

// Factory form (not bare automock): see ForgotPasswordPage.test.tsx.
vi.mock('@/features/identity/auth/api/resetPassword', () => ({
  resetPassword: vi.fn(),
}));

const mockedResetPassword = vi.mocked(resetPassword);

function renderPage(initialPath = '/reset-password?token=the-token') {
  const queryClient = new QueryClient({ defaultOptions: { queries: { retry: false } } });
  render(
    <QueryClientProvider client={queryClient}>
      <ThemeProvider>
        <MemoryRouter initialEntries={[initialPath]}>
          <ResetPasswordPage />
        </MemoryRouter>
      </ThemeProvider>
    </QueryClientProvider>,
  );
}

async function fillNewPassword(user: ReturnType<typeof userEvent.setup>, password: string, confirmation = password) {
  await user.type(screen.getByLabelText('Nouveau mot de passe'), password);
  await user.type(screen.getByLabelText('Confirmation du mot de passe'), confirmation);
  await user.click(screen.getByRole('button', { name: 'Réinitialiser mon mot de passe' }));
}

describe('ResetPasswordPage', () => {
  beforeEach(() => {
    mockedResetPassword.mockReset();
    mockedResetPassword.mockResolvedValue(undefined);
  });

  it('réinitialise puis annonce que les autres appareils sont déconnectés', async () => {
    const user = userEvent.setup();
    renderPage();

    await fillNewPassword(user, 'motdepasse123');

    // mock.calls[0][0] plutôt que toHaveBeenCalledWith : react-query passe un
    // second argument de contexte à la mutationFn.
    await waitFor(() =>
      expect(mockedResetPassword.mock.calls[0][0]).toEqual({ token: 'the-token', newPassword: 'motdepasse123' }),
    );
    expect(await screen.findByText(/Votre mot de passe a bien été réinitialisé/)).toBeInTheDocument();
    // La révocation des sessions se dit : c'est ce que l'utilisateur venu
    // reprendre son compte a besoin de savoir (voir ResetPasswordService).
    expect(screen.getByText(/appareils ont été déconnectés/)).toBeInTheDocument();
    expect(screen.getByRole('link', { name: 'Retour à la connexion' })).toHaveAttribute('href', '/login');
  });

  it('sans jeton dans l’URL, n’affiche aucun formulaire et n’appelle rien', () => {
    renderPage('/reset-password');

    expect(screen.getByText('Ce lien de réinitialisation est invalide.')).toBeInTheDocument();
    expect(screen.queryByLabelText('Nouveau mot de passe')).not.toBeInTheDocument();
    expect(mockedResetPassword).not.toHaveBeenCalled();
    // Une impasse serait cruelle : le lien coupé par la messagerie est le cas le
    // plus fréquent, et il se répare en redemandant un email.
    expect(screen.getByRole('link', { name: 'Demandez-en un nouveau' })).toHaveAttribute('href', '/forgot-password');
  });

  it('affiche le message de l’API quand le lien a expiré, sans fermer la porte', async () => {
    const user = userEvent.setup();
    mockedResetPassword.mockRejectedValue(new Error('Ce lien de réinitialisation a expiré. Demandez-en un nouveau.'));
    renderPage();

    await fillNewPassword(user, 'motdepasse123');

    expect(await screen.findByText(/Ce lien de réinitialisation a expiré/)).toBeInTheDocument();
    expect(screen.getByRole('link', { name: 'Retour à la connexion' })).toHaveAttribute('href', '/login');
  });

  it('bloque une confirmation qui ne correspond pas, avant tout appel', async () => {
    const user = userEvent.setup();
    renderPage();

    await fillNewPassword(user, 'motdepasse123', 'motdepasse124');

    expect(await screen.findByText('Les mots de passe ne correspondent pas')).toBeInTheDocument();
    expect(mockedResetPassword).not.toHaveBeenCalled();
  });

  it('refuse un mot de passe trop court, comme l’API', async () => {
    const user = userEvent.setup();
    renderPage();

    await fillNewPassword(user, 'court');

    expect(await screen.findByText('Le mot de passe doit contenir au moins 10 caractères')).toBeInTheDocument();
    expect(mockedResetPassword).not.toHaveBeenCalled();
  });
});
