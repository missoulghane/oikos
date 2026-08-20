import { beforeEach, describe, expect, it, vi } from 'vitest';
import { render, screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { MemoryRouter, Outlet, Route, Routes } from 'react-router-dom';
import { InstallmentsConfigurationTab } from '@/features/property-mngt/pricing/pages/InstallmentsConfigurationTab';
import { useUnitTypeDefinitions } from '@/features/property-mngt/properties/hooks/useUnitTypeDefinitions';
import { useUnitTypePrices } from '@/features/property-mngt/pricing/hooks/useUnitTypePrices';
import { useSetUnitTypePrice } from '@/features/property-mngt/pricing/hooks/useSetUnitTypePrice';
import { useSetProjectedBudget } from '@/features/property-mngt/pricing/hooks/useSetProjectedBudget';
import { useUpdateDuesCalculationMode } from '@/features/property-mngt/pricing/hooks/useUpdateDuesCalculationMode';
import type { Property } from '@/features/property-mngt/properties/types/property.types';

vi.mock('@/features/property-mngt/properties/hooks/useUnitTypeDefinitions', () => ({
  useUnitTypeDefinitions: vi.fn(),
}));
vi.mock('@/features/property-mngt/pricing/hooks/useUnitTypePrices', () => ({ useUnitTypePrices: vi.fn() }));
vi.mock('@/features/property-mngt/pricing/hooks/useSetUnitTypePrice', () => ({ useSetUnitTypePrice: vi.fn() }));
vi.mock('@/features/property-mngt/pricing/hooks/useSetProjectedBudget', () => ({ useSetProjectedBudget: vi.fn() }));
vi.mock('@/features/property-mngt/pricing/hooks/useUpdateDuesCalculationMode', () => ({
  useUpdateDuesCalculationMode: vi.fn(),
}));

const baseProperty: Property = {
  id: 'p-1',
  name: 'Résidence Les Oliviers',
  address: '12 rue de la Paix',
  city: 'Casablanca',
  duesCalculationMode: 'FLAT_RATE',
  projectedBudget: null,
};

const setModeAsync = vi.fn().mockResolvedValue(undefined);
const setBudgetAsync = vi.fn().mockResolvedValue(undefined);
const setPriceAsync = vi.fn().mockResolvedValue(undefined);

interface SetupOptions {
  property?: Partial<Property>;
  definitions?: { id: string; name: string }[];
  prices?: { unitTypeId: string; price: number }[];
}

function setup({ property = {}, definitions = [{ id: 'type-appart', name: 'Appartement' }], prices = [] }: SetupOptions = {}) {
  vi.mocked(useUnitTypeDefinitions).mockReturnValue({
    data: definitions.map((definition) => ({ ...definition, propertyId: 'p-1' })),
    isLoading: false,
    isError: false,
    error: null,
  } as never);
  vi.mocked(useUnitTypePrices).mockReturnValue({
    data: prices,
    isLoading: false,
    isError: false,
    error: null,
  } as never);
  vi.mocked(useUpdateDuesCalculationMode).mockReturnValue({ mutateAsync: setModeAsync, error: null } as never);
  vi.mocked(useSetProjectedBudget).mockReturnValue({ mutateAsync: setBudgetAsync, error: null } as never);
  vi.mocked(useSetUnitTypePrice).mockReturnValue({ mutateAsync: setPriceAsync, error: null } as never);

  render(
    <MemoryRouter initialEntries={['/configuration']}>
      <Routes>
        <Route path="/" element={<Outlet context={{ property: { ...baseProperty, ...property } }} />}>
          <Route path="configuration" element={<InstallmentsConfigurationTab />} />
        </Route>
      </Routes>
    </MemoryRouter>,
  );
}

describe('InstallmentsConfigurationTab', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  it('réunit le mode et les prix sous un seul bouton', () => {
    setup();

    expect(screen.getByRole('radio', { name: 'Forfait' })).toBeChecked();
    expect(screen.getByLabelText('Appartement')).toBeInTheDocument();
    expect(screen.getAllByRole('button', { name: 'Enregistrer' })).toHaveLength(1);
  });

  it('affiche 0 pour un type de lot sans prix', async () => {
    setup({ definitions: [{ id: 'type-appart', name: 'Appartement' }], prices: [] });

    await waitFor(() => expect(screen.getByLabelText('Appartement')).toHaveValue(0));
  });

  it('bascule l’affichage dès qu’on choisit les tantièmes, sans enregistrer', async () => {
    const user = userEvent.setup();
    setup();

    await user.click(screen.getByRole('radio', { name: 'Tantième' }));

    expect(screen.getByLabelText('Budget prévisionnel annuel')).toBeInTheDocument();
    expect(screen.queryByLabelText('Appartement')).not.toBeInTheDocument();
    expect(setModeAsync).not.toHaveBeenCalled();
  });

  it('enregistre le mode et les prix modifiés en une fois', async () => {
    const user = userEvent.setup();
    setup({ definitions: [{ id: 'type-appart', name: 'Appartement' }], prices: [{ unitTypeId: 'type-appart', price: 100 }] });

    await waitFor(() => expect(screen.getByLabelText('Appartement')).toHaveValue(100));
    await user.clear(screen.getByLabelText('Appartement'));
    await user.type(screen.getByLabelText('Appartement'), '150');
    await user.click(screen.getByRole('button', { name: 'Enregistrer' }));

    await waitFor(() => expect(setPriceAsync).toHaveBeenCalledWith({ unitTypeId: 'type-appart', price: 150 }));
    // Le mode n'a pas bougé : rien ne part de ce côté-là.
    expect(setModeAsync).not.toHaveBeenCalled();
  });

  it('envoie le budget prévisionnel quand on passe aux tantièmes', async () => {
    const user = userEvent.setup();
    setup();

    await user.click(screen.getByRole('radio', { name: 'Tantième' }));
    await user.clear(screen.getByLabelText('Budget prévisionnel annuel'));
    await user.type(screen.getByLabelText('Budget prévisionnel annuel'), '120000');
    await user.click(screen.getByRole('button', { name: 'Enregistrer' }));

    await waitFor(() => expect(setModeAsync).toHaveBeenCalledWith('SHARES'));
    expect(setBudgetAsync).toHaveBeenCalledWith(120000);
    expect(setPriceAsync).not.toHaveBeenCalled();
  });

  it('renvoie vers l’onglet des types quand la copropriété n’en a aucun', () => {
    setup({ definitions: [] });

    expect(screen.getByText(/Cochez les types de lots de votre copropriété/)).toBeInTheDocument();
  });
});
