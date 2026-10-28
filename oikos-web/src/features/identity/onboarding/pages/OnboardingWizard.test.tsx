import { beforeEach, describe, expect, it, vi } from 'vitest';
import { render, screen, waitFor, within } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { MemoryRouter, Route, Routes } from 'react-router-dom';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { ThemeProvider } from '@/shared/context/ThemeContext';
import { OnboardingWizardPage } from '@/features/identity/onboarding/pages/OnboardingWizardPage';
import { registerPropertyBoardAdmin } from '@/features/identity/register/api/registerPropertyBoardAdmin';
import { configureProperty } from '@/features/identity/onboarding/api/configureProperty';
import { createBoardAdminProperty } from '@/features/identity/onboarding/api/createBoardAdminProperty';

// Factory form (not bare automock): a bare `vi.mock(path)` still loads the
// real module to derive its shape, which would drag in httpClient -> env.ts
// (VITE_API_URL) with no .env available under Vitest.
vi.mock('@/features/identity/register/api/registerPropertyBoardAdmin', () => ({
  registerPropertyBoardAdmin: vi.fn(),
}));
vi.mock('@/features/identity/onboarding/api/configureProperty', () => ({
  configureProperty: vi.fn(),
}));
vi.mock('@/features/identity/onboarding/api/createBoardAdminProperty', () => ({
  createBoardAdminProperty: vi.fn(),
}));

const mockedRegister = vi.mocked(registerPropertyBoardAdmin);
const mockedConfigure = vi.mocked(configureProperty);
const mockedCreateProperty = vi.mocked(createBoardAdminProperty);

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
  await user.click(screen.getByRole('checkbox', { name: /J'accepte les conditions générales/ }));
  await user.click(screen.getByRole('button', { name: 'Continuer' }));
  // Même raison que fillPropertyStep : la navigation part du onSuccess de la
  // création du compte, après la résolution du clic.
  await screen.findByText('Parlez-nous de votre copropriété');
}

async function fillPropertyStep(user: ReturnType<typeof userEvent.setup>) {
  await user.type(await screen.findByLabelText('Nom de la copropriété'), 'Résidence Exemple');
  await user.type(screen.getByLabelText('Adresse'), '12 rue Exemple');
  await user.type(screen.getByLabelText('Ville'), 'Casablanca');
  await user.click(screen.getByRole('button', { name: 'Continuer' }));
  // Waits for step 3 before returning, and not out of politeness: this step
  // navigates from the property mutation's onSuccess (see PropertyStepPage), so
  // the click resolves before the wizard has moved. Callers that then look for
  // "Continuer" would otherwise match step 2's button, still mounted, re-submit
  // it, and end up asserting against step 3 believing they were on step 4.
  await screen.findByText('Quels types de lots gérez-vous ?');
}

/**
 * Types de lots (Appartement, coché par défaut), mode forfait (le défaut), puis
 * le montant de l'appartement : le chemin le plus court jusqu'aux bâtiments.
 */
async function fillUnitTypesModeAndPricing(user: ReturnType<typeof userEvent.setup>) {
  await user.click(await screen.findByRole('button', { name: 'Continuer' }));
  await screen.findByText('Comment souhaitez-vous gérer vos charges ?');
  await user.click(await screen.findByRole('button', { name: 'Continuer' }));
  await screen.findByText('Quel montant pour chaque type de lot ?');
  await user.type(await screen.findByLabelText('Montant Appartement'), '50');
  await user.click(await screen.findByRole('button', { name: 'Continuer' }));
  await screen.findByText('Structure de votre copropriété');
}

/**
 * Joue le tunnel jusqu'au récapitulatif, puis revient sur l'étape 2 par son
 * lien « Modifier » : le seul chemin, depuis que le brouillon ne survit plus à
 * un rechargement, pour retrouver l'étape 2 avec une inscription déjà faite.
 */
async function goBackToPropertyStepFromRecap(user: ReturnType<typeof userEvent.setup>) {
  await fillAccountStep(user);
  await fillPropertyStep(user);
  await fillUnitTypesModeAndPricing(user);
  await user.click(await screen.findByRole('button', { name: 'Continuer' }));
  await user.click(await screen.findByRole('button', { name: 'Passer cette étape' }));

  const propertySection = (await screen.findByText('Votre copropriété')).closest('section')!;
  await user.click(within(propertySection).getByRole('link', { name: 'Modifier' }));
}

describe('OnboardingWizardPage', () => {
  beforeEach(() => {
    mockedRegister.mockReset();
    mockedConfigure.mockReset();
    mockedCreateProperty.mockReset();
    mockedRegister.mockResolvedValue({
      userId: 'user-1',
      onboardingToken: 'account-token',
      expiresInSeconds: 7200,
    });
    mockedCreateProperty.mockResolvedValue({
      propertyId: 'property-1',
      onboardingToken: 'onboarding-token',
      expiresInSeconds: 7200,
    });
    mockedConfigure.mockResolvedValue(undefined);
  });

  it('creates the account on step 1, alone, with the terms accepted', async () => {
    const user = userEvent.setup();
    renderWizard();

    await fillAccountStep(user);

    expect(mockedRegister).toHaveBeenCalledTimes(1);
    expect(mockedRegister.mock.calls[0][0]).toEqual({
      fullName: 'Jane Doe',
      email: 'jane.doe@example.com',
      // Saisi « 0612345678 » sous l'indicatif marocain : le 0 de tête n'existe
      // pas en format international, c'est PhoneField qui l'écarte.
      phone: '+212612345678',
      password: 'motdepasse123',
      acceptedTerms: true,
    });
    // La copropriété ne vient qu'à l'étape 2.
    expect(mockedCreateProperty).not.toHaveBeenCalled();
  });

  it('refuses to create the account until the terms are accepted', async () => {
    const user = userEvent.setup();
    renderWizard();

    await user.type(screen.getByLabelText('Nom complet'), 'Jane Doe');
    await user.type(screen.getByLabelText('Adresse email'), 'jane.doe@example.com');
    await user.type(screen.getByLabelText('Téléphone'), '0612345678');
    await user.type(screen.getByLabelText('Mot de passe'), 'motdepasse123');
    await user.type(screen.getByLabelText('Confirmation du mot de passe'), 'motdepasse123');
    await user.click(screen.getByRole('button', { name: 'Continuer' }));

    expect(await screen.findByText("Vous devez accepter les conditions générales d'utilisation et de vente")).toBeInTheDocument();
    expect(mockedRegister).not.toHaveBeenCalled();
  });

  it('links the terms to their pages, in a new tab so the step keeps what was typed', () => {
    renderWizard();

    const termsOfUse = screen.getByRole('link', { name: "conditions générales d'utilisation" });
    const termsOfSale = screen.getByRole('link', { name: 'conditions générales de vente' });
    expect(termsOfUse).toHaveAttribute('href', '/cgu');
    expect(termsOfSale).toHaveAttribute('href', '/cgv');
    expect(termsOfUse).toHaveAttribute('target', '_blank');
    expect(termsOfSale).toHaveAttribute('target', '_blank');
  });

  it('offers the board seats with « Syndic » selected by default', async () => {
    renderWizard();

    const seat = screen.getByLabelText('Votre rôle dans le conseil syndical');
    expect(seat).toHaveDisplayValue('Syndic');
    expect(within(seat).getAllByRole('option').map((option) => option.textContent)).toEqual([
      'Syndic',
      'Président',
      'Trésorier',
      'Secrétaire',
      'Membre du conseil syndical',
    ]);
  });

  it('signals two different passwords on the spot, without submitting the step', async () => {
    const user = userEvent.setup();
    renderWizard();

    await user.type(screen.getByLabelText('Mot de passe'), 'motdepasse123');
    await user.type(screen.getByLabelText('Confirmation du mot de passe'), 'motdepasse124');
    // Sortie du champ : ni clic sur « Continuer », ni appel réseau.
    await user.tab();

    expect(await screen.findByText('Les mots de passe ne correspondent pas')).toBeInTheDocument();
    expect(mockedRegister).not.toHaveBeenCalled();
  });

  it('keeps the password rule out of sight until it is actually broken', async () => {
    const user = userEvent.setup();
    renderWizard();

    expect(screen.queryByText('Au moins 10 caractères')).not.toBeInTheDocument();

    await user.type(screen.getByLabelText('Mot de passe'), 'court');
    await user.tab();

    expect(await screen.findByText('Au moins 10 caractères')).toBeInTheDocument();
  });

  it('creates the property on step 2 with the seat chosen on step 1, authenticated by the account token', async () => {
    const user = userEvent.setup();
    renderWizard();

    await user.selectOptions(screen.getByLabelText('Votre rôle dans le conseil syndical'), 'Trésorier');
    await fillAccountStep(user);
    await user.type(await screen.findByLabelText('Nom de la copropriété'), 'Résidence Exemple');
    await user.type(screen.getByLabelText('Adresse'), '12 rue Exemple');
    await user.type(screen.getByLabelText('Ville'), 'Casablanca');
    // Le siège a été choisi à l'étape 1 : il n'est pas redemandé ici.
    expect(screen.queryByLabelText('Votre rôle dans le conseil syndical')).not.toBeInTheDocument();
    await user.click(screen.getByRole('button', { name: 'Continuer' }));

    await waitFor(() => expect(mockedCreateProperty).toHaveBeenCalled());
    expect(mockedCreateProperty.mock.calls[0][0]).toEqual({
      onboardingToken: 'account-token',
      payload: {
        propertyName: 'Résidence Exemple',
        // Deux champs distincts et non plus une concaténation : la fiche
        // copropriété doit pouvoir afficher et corriger l'un sans l'autre.
        propertyAddress: '12 rue Exemple',
        propertyCity: 'Casablanca',
        boardRole: 'TREASURER',
      },
    });
    expect(mockedRegister).toHaveBeenCalledTimes(1);
  });

  it('does not recreate the account when going back to step 1', async () => {
    const user = userEvent.setup();
    renderWizard();
    await fillAccountStep(user);

    await user.click(screen.getByRole('link', { name: /Revenir à l'étape précédente/ }));

    expect(await screen.findByText('Votre compte est créé')).toBeInTheDocument();
    await user.click(screen.getByRole('button', { name: 'Continuer' }));
    expect(await screen.findByText('Parlez-nous de votre copropriété')).toBeInTheDocument();
    expect(mockedRegister).toHaveBeenCalledTimes(1);
  });

  it('asks the unit types, then the mode, then the amounts in flat-rate mode or the budget in shares mode', async () => {
    const user = userEvent.setup();
    renderWizard();
    await fillAccountStep(user);
    await fillPropertyStep(user);

    // Les types se choisissent seuls, sans montant à côté.
    expect(screen.queryByLabelText('Montant Appartement')).not.toBeInTheDocument();
    expect(screen.getByText('Étape 3 sur 8')).toBeInTheDocument();
    await user.click(screen.getByRole('button', { name: 'Continuer' }));

    // Forfait est le choix par défaut.
    expect(await screen.findByText('Comment souhaitez-vous gérer vos charges ?')).toBeInTheDocument();
    expect(screen.getByRole('radio', { name: /Forfait/ })).toBeChecked();
    await user.click(screen.getByRole('button', { name: 'Continuer' }));

    expect(await screen.findByText('Quel montant pour chaque type de lot ?')).toBeInTheDocument();
    expect(screen.getByLabelText('Montant Appartement')).toBeInTheDocument();
    expect(screen.queryByLabelText('Budget prévisionnel annuel')).not.toBeInTheDocument();
    expect(screen.getByText('Étape 5 sur 8')).toBeInTheDocument();

    // Retour au mode pour basculer en tantièmes : même étape, autre contenu, même total.
    await user.click(screen.getByRole('link', { name: /Revenir à l'étape précédente/ }));
    await user.click(await screen.findByRole('radio', { name: /Tantièmes/ }));
    await user.click(screen.getByRole('button', { name: 'Continuer' }));

    expect(await screen.findByText('Quel est votre budget prévisionnel ?')).toBeInTheDocument();
    expect(screen.getByLabelText('Budget prévisionnel annuel')).toBeInTheDocument();
    expect(screen.queryByLabelText('Montant Appartement')).not.toBeInTheDocument();
    expect(screen.getByText('Étape 5 sur 8')).toBeInTheDocument();
  });

  it('requires an amount for every selected unit type in flat-rate mode', async () => {
    const user = userEvent.setup();
    renderWizard();
    await fillAccountStep(user);
    await fillPropertyStep(user);
    await user.click(screen.getByRole('button', { name: 'Continuer' }));
    await screen.findByText('Comment souhaitez-vous gérer vos charges ?');
    await user.click(screen.getByRole('button', { name: 'Continuer' }));

    await screen.findByText('Quel montant pour chaque type de lot ?');
    await user.click(screen.getByRole('button', { name: 'Continuer' }));

    expect(await screen.findByText('Le montant est requis')).toBeInTheDocument();
    expect(screen.queryByText('Structure de votre copropriété')).not.toBeInTheDocument();
  });

  it('shows exactly the selected unit types on the buildings step', async () => {
    const user = userEvent.setup();
    renderWizard();
    await fillAccountStep(user);
    await fillPropertyStep(user);

    // Appartement est coché par défaut ; on ajoute Box, pas Bureau.
    await user.click(await screen.findByRole('checkbox', { name: /Box/ }));
    await user.click(screen.getByRole('button', { name: 'Continuer' }));
    await screen.findByText('Comment souhaitez-vous gérer vos charges ?');
    await user.click(screen.getByRole('button', { name: 'Continuer' }));
    // Un montant par type coché, et seulement pour eux.
    await screen.findByText('Quel montant pour chaque type de lot ?');
    expect(screen.queryByLabelText('Montant Bureau')).not.toBeInTheDocument();
    await user.type(screen.getByLabelText('Montant Appartement'), '50');
    await user.type(screen.getByLabelText('Montant Box'), '20');
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
    // Types de lots : Appartement seul ; mode forfait (le défaut) ; montant 50.
    await fillUnitTypesModeAndPricing(user);
    // Bâtiments : 12 appartements dans l'unique bâtiment, sur 3 étages.
    const floorCount = await screen.findByRole('spinbutton', { name: "Nombre d'étages" });
    await user.clear(floorCount);
    await user.type(floorCount, '3');
    const apartmentCount = screen.getByRole('spinbutton', { name: 'Appartement' });
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
        buildings: [
          { name: 'Bâtiment principal', floorCount: 3, unitTypes: [{ unitTypeName: 'Appartement', count: 12 }] },
        ],
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
    await fillUnitTypesModeAndPricing(user);
    await user.click(await screen.findByRole('button', { name: 'Continuer' }));
    await user.click(await screen.findByRole('button', { name: 'Passer cette étape' }));

    const propertySection = (await screen.findByText('Votre copropriété')).closest('section')!;
    await user.click(within(propertySection).getByRole('link', { name: 'Modifier' }));

    expect(await screen.findByLabelText('Nom de la copropriété')).toHaveValue('Résidence Exemple');
    // Revenir en arrière ne recrée ni le compte ni la copropriété : ils existent déjà.
    expect(mockedRegister).toHaveBeenCalledTimes(1);
    expect(mockedCreateProperty).toHaveBeenCalledTimes(1);
  });

  /**
   * La page de fin efface le brouillon en se montant. Elle affiche pourtant
   * l'email qu'elle vient d'effacer : sans la copie figée avant le nettoyage,
   * le dernier écran du tunnel annonce un email vide.
   */
  it('affiche l’email de confirmation bien qu’elle efface le brouillon', async () => {
    const user = userEvent.setup();
    renderWizard();
    await fillAccountStep(user);
    await fillPropertyStep(user);
    await fillUnitTypesModeAndPricing(user);
    await user.click(await screen.findByRole('button', { name: 'Continuer' }));
    await user.click(await screen.findByRole('button', { name: 'Passer cette étape' }));
    await user.click(await screen.findByRole('button', { name: 'Finaliser ma copropriété' }));

    expect(await screen.findByText('Votre copropriété est créée !')).toBeInTheDocument();
    expect(screen.getByText('jane.doe@example.com')).toBeInTheDocument();
  });

  // Revenir sur l'étape 2 après coup - par le récapitulatif - retrouve une
  // inscription déjà faite. Continuer la reprend, c'est voulu, mais plus en
  // silence : le nom saisi ici ne repartirait nulle part.
  it('annonce la reprise d’une inscription déjà faite', async () => {
    const user = userEvent.setup();
    renderWizard();
    await goBackToPropertyStepFromRecap(user);

    expect(await screen.findByText(/Une copropriété a déjà été créée/)).toBeInTheDocument();
    expect(screen.getByRole('button', { name: 'Recommencer avec une nouvelle copropriété' })).toBeInTheDocument();
  });

  it('repart de zéro sans recréer ce qui existe déjà', async () => {
    const user = userEvent.setup();
    renderWizard();
    await goBackToPropertyStepFromRecap(user);

    await user.click(await screen.findByRole('button', { name: 'Recommencer avec une nouvelle copropriété' }));

    // Le brouillon est vidé ; le compte et la copropriété d'avant, eux, existent
    // toujours - on ne les recrée pas.
    expect(
      screen.queryByRole('button', { name: 'Recommencer avec une nouvelle copropriété' }),
    ).not.toBeInTheDocument();
    expect(mockedRegister).toHaveBeenCalledTimes(1);
    expect(mockedCreateProperty).toHaveBeenCalledTimes(1);
    // Repartir de zéro oublie aussi le compte : l'étape 2 renvoie à l'étape 1
    // plutôt que de créer une copropriété sans compte pour la porter.
    expect(screen.getByText(/Commencez par créer votre compte/)).toBeInTheDocument();
    expect(screen.getByRole('button', { name: 'Continuer' })).toBeDisabled();
  });
});
