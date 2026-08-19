import { beforeEach, describe, expect, it, vi } from 'vitest';
import { render, screen, waitFor, within } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { MemoryRouter, Route, Routes } from 'react-router-dom';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { ThemeProvider } from '@/shared/context/ThemeContext';
import { OnboardingWizardPage } from '@/features/identity/onboarding/pages/OnboardingWizardPage';
import { registerPropertyBoardAdmin } from '@/features/identity/register/api/registerPropertyBoardAdmin';
import { configureProperty } from '@/features/identity/onboarding/api/configureProperty';
import { captureOnboardingLead } from '@/features/identity/onboarding/api/captureOnboardingLead';

// Factory form (not bare automock): a bare `vi.mock(path)` still loads the
// real module to derive its shape, which would drag in httpClient -> env.ts
// (VITE_API_URL) with no .env available under Vitest.
vi.mock('@/features/identity/register/api/registerPropertyBoardAdmin', () => ({
  registerPropertyBoardAdmin: vi.fn(),
}));
vi.mock('@/features/identity/onboarding/api/configureProperty', () => ({
  configureProperty: vi.fn(),
}));
vi.mock('@/features/identity/onboarding/api/captureOnboardingLead', () => ({
  captureOnboardingLead: vi.fn(),
}));

const mockedRegister = vi.mocked(registerPropertyBoardAdmin);
const mockedConfigure = vi.mocked(configureProperty);
const mockedCaptureLead = vi.mocked(captureOnboardingLead);

function renderWizard(initialPath = '/register/board-admin/account') {
  const queryClient = new QueryClient({ defaultOptions: { queries: { retry: false } } });
  render(
    // AuthLayout embarque le sélecteur de thème, qui exige son provider.
    <QueryClientProvider client={queryClient}>
      <ThemeProvider>
        <MemoryRouter initialEntries={[initialPath]}>
          <Routes>
            <Route path="/register/board-admin/*" element={<OnboardingWizardPage />} />
          </Routes>
        </MemoryRouter>
      </ThemeProvider>
    </QueryClientProvider>,
  );
}

async function fillAccountStep(user: ReturnType<typeof userEvent.setup>) {
  await user.type(screen.getByLabelText('Nom complet'), 'Jane Doe');
  await user.type(screen.getByLabelText('Adresse email'), 'jane.doe@example.com');
  await user.type(screen.getByLabelText('Téléphone'), '0612345678');
  await user.type(screen.getByLabelText('Mot de passe'), 'motdepasse123');
  await user.type(screen.getByLabelText('Confirmation du mot de passe'), 'motdepasse123');
  await user.click(screen.getByRole('button', { name: 'Continuer' }));
}

async function fillPropertyStep(user: ReturnType<typeof userEvent.setup>) {
  await user.type(await screen.findByLabelText('Nom de la copropriété'), 'Résidence Exemple');
  await user.type(screen.getByLabelText('Adresse'), '12 rue Exemple');
  await user.type(screen.getByLabelText('Ville'), 'Casablanca');
  await user.click(screen.getByRole('button', { name: 'Continuer' }));
  // Waits for step 3 before returning, and not out of politeness: this step
  // navigates from the register mutation's onSuccess (see PropertyStepPage), so
  // the click resolves before the wizard has moved. Callers that then look for
  // "Continuer" would otherwise match step 2's button, still mounted, re-submit
  // it, and end up asserting against step 3 believing they were on step 4.
  await screen.findByText('Comment souhaitez-vous gérer vos charges ?');
}

describe('OnboardingWizardPage', () => {
  beforeEach(() => {
    window.localStorage.clear();
    mockedRegister.mockReset();
    mockedConfigure.mockReset();
    mockedCaptureLead.mockReset();
    mockedRegister.mockResolvedValue({
      userId: 'user-1',
      propertyId: 'property-1',
      onboardingToken: 'onboarding-token',
      expiresInSeconds: 7200,
    });
    mockedConfigure.mockResolvedValue(undefined);
    mockedCaptureLead.mockResolvedValue(undefined);
  });

  it('reprend un brouillon écrit avant l’ajout du téléphone, sans tomber', async () => {
    // Ce brouillon-là existe pour de vrai dans les navigateurs ouverts avant que
    // le champ n'existe : `account` y tient en deux clés, et le remplacement en
    // bloc des valeurs par défaut laissait `phone` à `undefined`, ce que
    // PhoneField faisait payer d'un écran blanc.
    window.localStorage.setItem(
      'oikos-onboarding-draft',
      JSON.stringify({ account: { fullName: 'Jane Doe', email: 'jane.doe@example.com' } }),
    );

    renderWizard();

    expect(await screen.findByLabelText('Nom complet')).toHaveValue('Jane Doe');
    expect(screen.getByLabelText('Téléphone')).toHaveValue('');
  });

  it('captures the email on step 1, before any account can exist', async () => {
    const user = userEvent.setup();
    renderWizard();

    await fillAccountStep(user);

    await waitFor(() => expect(mockedCaptureLead).toHaveBeenCalled());
    expect(mockedCaptureLead.mock.calls[0][0]).toEqual({
      email: 'jane.doe@example.com',
      fullName: 'Jane Doe',
    });
    expect(mockedRegister).not.toHaveBeenCalled();
    expect(await screen.findByText('Parlez-nous de votre copropriété')).toBeInTheDocument();
  });

  it('signals two different passwords on the spot, without submitting the step', async () => {
    const user = userEvent.setup();
    renderWizard();

    await user.type(screen.getByLabelText('Mot de passe'), 'motdepasse123');
    await user.type(screen.getByLabelText('Confirmation du mot de passe'), 'motdepasse124');
    // Sortie du champ : ni clic sur « Continuer », ni appel réseau.
    await user.tab();

    expect(await screen.findByText('Les mots de passe ne correspondent pas')).toBeInTheDocument();
    expect(mockedCaptureLead).not.toHaveBeenCalled();
  });

  it('keeps the password rule out of sight until it is actually broken', async () => {
    const user = userEvent.setup();
    renderWizard();

    expect(screen.queryByText('Au moins 10 caractères')).not.toBeInTheDocument();

    await user.type(screen.getByLabelText('Mot de passe'), 'court');
    await user.tab();

    expect(await screen.findByText('Au moins 10 caractères')).toBeInTheDocument();
  });

  it('creates the account at the end of step 2, with the address recomposed into a single field', async () => {
    const user = userEvent.setup();
    renderWizard();

    await fillAccountStep(user);
    await user.type(await screen.findByLabelText('Nom de la copropriété'), 'Résidence Exemple');
    await user.type(screen.getByLabelText('Adresse'), '12 rue Exemple');
    await user.type(screen.getByLabelText('Ville'), 'Casablanca');
    await user.click(screen.getByRole('button', { name: 'Continuer' }));

    await waitFor(() => expect(mockedRegister).toHaveBeenCalled());
    expect(mockedRegister.mock.calls[0][0]).toEqual(
      expect.objectContaining({
        fullName: 'Jane Doe',
        email: 'jane.doe@example.com',
        // Saisi « 0612345678 » sous l'indicatif marocain : le 0 de tête n'existe
        // pas en format international, c'est PhoneField qui l'écarte.
        phone: '+212612345678',
        propertyName: 'Résidence Exemple',
        propertyAddress: '12 rue Exemple, Casablanca',
      }),
    );
  });

  it('offers the flat-rate amounts only, and the projected budget in shares mode', async () => {
    const user = userEvent.setup();
    renderWizard();
    await fillAccountStep(user);
    await fillPropertyStep(user);

    // Forfait est le choix par défaut.
    expect(await screen.findByText('Comment souhaitez-vous gérer vos charges ?')).toBeInTheDocument();
    expect(screen.getByRole('radio', { name: /Forfait/ })).toBeChecked();
    await user.click(screen.getByRole('button', { name: 'Continuer' }));

    expect(await screen.findByText('Quels types de lots gérez-vous ?')).toBeInTheDocument();
    expect(screen.getByText('Montant par type de lot')).toBeInTheDocument();
    expect(screen.queryByText('Budget prévisionnel')).not.toBeInTheDocument();

    // Retour à l'étape précédente pour basculer en tantièmes.
    await user.click(screen.getByRole('link', { name: /Revenir à l'étape précédente/ }));
    await user.click(await screen.findByRole('radio', { name: /Tantièmes/ }));
    await user.click(screen.getByRole('button', { name: 'Continuer' }));

    expect(await screen.findByText('Budget prévisionnel')).toBeInTheDocument();
    expect(screen.queryByText('Montant par type de lot')).not.toBeInTheDocument();
  });

  it('shows exactly the selected unit types on the buildings step', async () => {
    const user = userEvent.setup();
    renderWizard();
    await fillAccountStep(user);
    await fillPropertyStep(user);
    await user.click(await screen.findByRole('button', { name: 'Continuer' }));

    // Appartement est coché par défaut ; on ajoute Box, pas Bureau.
    await user.click(await screen.findByRole('checkbox', { name: /Box/ }));
    await user.click(screen.getByRole('button', { name: 'Continuer' }));

    expect(await screen.findByText('Structure de votre copropriété')).toBeInTheDocument();
    expect(screen.getByRole('spinbutton', { name: 'Appartement' })).toBeInTheDocument();
    expect(screen.getByRole('spinbutton', { name: 'Box' })).toBeInTheDocument();
    expect(screen.queryByRole('spinbutton', { name: 'Bureau' })).not.toBeInTheDocument();
  });

  it('sends the whole configuration in a single call, authenticated by the onboarding token', async () => {
    const user = userEvent.setup();
    renderWizard();
    await fillAccountStep(user);
    await fillPropertyStep(user);
    // Mode de gestion (forfait par défaut).
    await user.click(await screen.findByRole('button', { name: 'Continuer' }));
    // Types de lots : Appartement seul, à 50.
    await user.type(await screen.findByLabelText('Appartement'), '50');
    await user.click(screen.getByRole('button', { name: 'Continuer' }));
    // Bâtiments : 12 appartements dans l'unique bâtiment.
    const apartmentCount = await screen.findByRole('spinbutton', { name: 'Appartement' });
    await user.clear(apartmentCount);
    await user.type(apartmentCount, '12');
    await user.click(screen.getByRole('button', { name: 'Continuer' }));
    // Comptes bancaires : étape sautée.
    await user.click(await screen.findByRole('button', { name: 'Passer cette étape' }));

    expect(await screen.findByText('Vérifiez votre configuration')).toBeInTheDocument();
    await user.click(screen.getByRole('button', { name: 'Finaliser ma copropriété' }));

    await waitFor(() => expect(mockedConfigure).toHaveBeenCalled());
    expect(mockedConfigure.mock.calls[0][0]).toEqual({
      propertyId: 'property-1',
      onboardingToken: 'onboarding-token',
      payload: {
        duesCalculationMode: 'FLAT_RATE',
        projectedBudget: undefined,
        unitTypes: [{ name: 'Appartement', price: 50 }],
        buildings: [{ name: 'Bâtiment principal', unitTypes: [{ unitTypeName: 'Appartement', count: 12 }] }],
        bankAccounts: [],
      },
    });
    expect(await screen.findByText('Votre copropriété est créée !')).toBeInTheDocument();
  });

  it('lets the recap jump back to a step and keeps what was already entered', async () => {
    const user = userEvent.setup();
    renderWizard();
    await fillAccountStep(user);
    await fillPropertyStep(user);
    // Each click waits for the step it lands on before the next one looks for
    // "Continuer" again - otherwise a slow render lets it match the button of
    // the step we just left (the failure mode fillPropertyStep guards against).
    await user.click(await screen.findByRole('button', { name: 'Continuer' }));
    await screen.findByText('Quels types de lots gérez-vous ?');
    await user.click(await screen.findByRole('button', { name: 'Continuer' }));
    await screen.findByText('Structure de votre copropriété');
    await user.click(await screen.findByRole('button', { name: 'Continuer' }));
    await user.click(await screen.findByRole('button', { name: 'Passer cette étape' }));

    const propertySection = (await screen.findByText('Votre copropriété')).closest('section')!;
    await user.click(within(propertySection).getByRole('link', { name: 'Modifier' }));

    expect(await screen.findByLabelText('Nom de la copropriété')).toHaveValue('Résidence Exemple');
    // Revenir en arrière ne recrée pas le compte : il existe déjà.
    expect(mockedRegister).toHaveBeenCalledTimes(1);
  });

  it('resumes an interrupted wizard from the stored draft', async () => {
    const user = userEvent.setup();
    renderWizard();
    await fillAccountStep(user);
    await fillPropertyStep(user);
    await screen.findByText('Comment souhaitez-vous gérer vos charges ?');

    // Nouveau rendu, comme après un rechargement de page.
    renderWizard('/register/board-admin/summary');

    const sections = await screen.findAllByText('Résidence Exemple');
    expect(sections.length).toBeGreaterThan(0);
    expect(mockedRegister).toHaveBeenCalledTimes(1);
  });
});
