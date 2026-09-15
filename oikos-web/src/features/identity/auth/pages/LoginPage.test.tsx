import { beforeEach, describe, expect, it, vi } from 'vitest';
import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { MemoryRouter } from 'react-router-dom';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { AxiosError, AxiosHeaders } from 'axios';
import { ThemeProvider } from '@/shared/context/ThemeContext';
import { LoginPage } from '@/features/identity/auth/pages/LoginPage';
import { login } from '@/features/identity/auth/api/login';
import type { ApiErrorBody } from '@/shared/types/apiError.types';

// Factory form (voir ForgotPasswordPage.test.tsx) : un automock nu chargerait le
// vrai module, donc httpClient -> env.ts, sans .env sous Vitest.
vi.mock('@/features/identity/auth/api/login', () => ({
  login: vi.fn(),
}));

const mockedLogin = vi.mocked(login);

function apiError(status: number, message: string, code?: string): AxiosError<ApiErrorBody> {
  const error = new AxiosError<ApiErrorBody>('Request failed', String(status));
  error.response = {
    data: { status, error: 'Error', message, code, path: '/auth/login', timestamp: '' },
    status,
    statusText: 'Error',
    headers: {},
    config: { headers: new AxiosHeaders() },
  };
  return error;
}

function renderPage() {
  const queryClient = new QueryClient({
    defaultOptions: { queries: { retry: false }, mutations: { retry: false } },
  });
  render(
    <QueryClientProvider client={queryClient}>
      <ThemeProvider>
        <MemoryRouter>
          <LoginPage />
        </MemoryRouter>
      </ThemeProvider>
    </QueryClientProvider>,
  );
}

async function submitCredentials(user: ReturnType<typeof userEvent.setup>) {
  await user.type(screen.getByLabelText('Identifiant'), 'jane.doe@example.com');
  await user.type(screen.getByLabelText('Mot de passe'), 'motdepasse123');
  await user.click(screen.getByRole('button', { name: 'Se connecter' }));
}

describe('LoginPage', () => {
  beforeEach(() => {
    mockedLogin.mockReset();
  });

  it('refuse en français, sans dire lequel des deux champs est faux', async () => {
    const user = userEvent.setup();
    mockedLogin.mockRejectedValue(apiError(403, 'Invalid credentials', 'INVALID_CREDENTIALS'));
    renderPage();

    await submitCredentials(user);

    expect(await screen.findByText('Identifiant ou mot de passe invalide.')).toBeInTheDocument();
    // Le message de l'API est écrit pour les logs : il ne doit jamais atteindre
    // l'écran, ni en anglais ni sous forme de détail d'exception.
    expect(screen.queryByText(/Invalid credentials/)).not.toBeInTheDocument();
  });

  it('parle d’activation seulement quand l’API l’a confirmé après le mot de passe', async () => {
    const user = userEvent.setup();
    mockedLogin.mockRejectedValue(apiError(403, 'Account is not activated', 'ACCOUNT_NOT_ACTIVATED'));
    renderPage();

    await submitCredentials(user);

    expect(await screen.findByText(/n’est pas encore activé/)).toBeInTheDocument();
  });

  it('n’accuse pas les identifiants quand le serveur tombe, et laisse le support', async () => {
    const user = userEvent.setup();
    mockedLogin.mockRejectedValue(apiError(500, 'An unexpected error occurred'));
    renderPage();

    await submitCredentials(user);

    expect(await screen.findByText(/contactez le support/)).toBeInTheDocument();
    expect(screen.queryByText(/An unexpected error occurred/)).not.toBeInTheDocument();
  });
});
