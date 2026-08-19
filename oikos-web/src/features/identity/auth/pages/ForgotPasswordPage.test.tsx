import { beforeEach, describe, expect, it, vi } from 'vitest';
import { render, screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { MemoryRouter } from 'react-router-dom';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { ThemeProvider } from '@/shared/context/ThemeContext';
import { ForgotPasswordPage } from '@/features/identity/auth/pages/ForgotPasswordPage';
import { forgotPassword } from '@/features/identity/auth/api/forgotPassword';

// Factory form (not bare automock): a bare `vi.mock(path)` still loads the real
// module to derive its shape, which would drag in httpClient -> env.ts
// (VITE_API_URL) with no .env available under Vitest.
vi.mock('@/features/identity/auth/api/forgotPassword', () => ({
  forgotPassword: vi.fn(),
}));

const mockedForgotPassword = vi.mocked(forgotPassword);

function renderPage() {
  const queryClient = new QueryClient({ defaultOptions: { queries: { retry: false } } });
  render(
    // AuthLayout embarque le sélecteur de thème, qui exige son provider.
    <QueryClientProvider client={queryClient}>
      <ThemeProvider>
        <MemoryRouter>
          <ForgotPasswordPage />
        </MemoryRouter>
      </ThemeProvider>
    </QueryClientProvider>,
  );
}

describe('ForgotPasswordPage', () => {
  beforeEach(() => {
    mockedForgotPassword.mockReset();
    mockedForgotPassword.mockResolvedValue(undefined);
  });

  it('confirme l’envoi sans jamais dire si le compte existe', async () => {
    const user = userEvent.setup();
    renderPage();

    await user.type(screen.getByLabelText('Adresse email'), 'jane.doe@example.com');
    await user.click(screen.getByRole('button', { name: 'Envoyer le lien' }));

    expect(await screen.findByText('Vérifiez votre boîte mail')).toBeInTheDocument();
    // mock.calls[0][0] plutôt que toHaveBeenCalledWith : react-query passe un
    // second argument de contexte à la mutationFn (même parti pris que
    // OnboardingWizard.test.tsx).
    await waitFor(() => expect(mockedForgotPassword.mock.calls[0][0]).toEqual({ email: 'jane.doe@example.com' }));
    // La formulation conditionnelle est le pendant du 202 systématique de l'API :
    // un écran qui trancherait ferait de cette page un annuaire des comptes.
    expect(screen.getByText(/Si un compte existe pour cette adresse/)).toBeInTheDocument();
    expect(screen.queryByText(/inconnue/i)).not.toBeInTheDocument();
    expect(screen.queryByText(/jane\.doe@example\.com/)).not.toBeInTheDocument();
  });

  it('ne poste rien tant que l’adresse est invalide', async () => {
    const user = userEvent.setup();
    renderPage();

    await user.type(screen.getByLabelText('Adresse email'), 'pas-une-adresse');
    await user.click(screen.getByRole('button', { name: 'Envoyer le lien' }));

    expect(await screen.findByText('Email invalide')).toBeInTheDocument();
    expect(mockedForgotPassword).not.toHaveBeenCalled();
  });

  it('laisse toujours un chemin vers la connexion', async () => {
    renderPage();

    expect(screen.getByRole('link', { name: 'Retour à la connexion' })).toHaveAttribute('href', '/login');
  });
});
