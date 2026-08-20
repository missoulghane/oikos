import { beforeEach, describe, expect, it, vi } from 'vitest';
import { render, screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { MemoryRouter, Outlet, Route, Routes } from 'react-router-dom';
import { PropertyUnitTypesTab } from '@/features/property-mngt/properties/pages/PropertyUnitTypesTab';
import { useUnitTypeDefinitions } from '@/features/property-mngt/properties/hooks/useUnitTypeDefinitions';
import { useAddUnitTypeDefinition } from '@/features/property-mngt/properties/hooks/useAddUnitTypeDefinition';
import { useRemoveUnitTypeDefinition } from '@/features/property-mngt/properties/hooks/useRemoveUnitTypeDefinition';
import type { Property } from '@/features/property-mngt/properties/types/property.types';

vi.mock('@/features/property-mngt/properties/hooks/useUnitTypeDefinitions', () => ({
  useUnitTypeDefinitions: vi.fn(),
}));
vi.mock('@/features/property-mngt/properties/hooks/useAddUnitTypeDefinition', () => ({
  useAddUnitTypeDefinition: vi.fn(),
}));
vi.mock('@/features/property-mngt/properties/hooks/useRemoveUnitTypeDefinition', () => ({
  useRemoveUnitTypeDefinition: vi.fn(),
}));

const property: Property = {
  id: 'p-1',
  name: 'Résidence Les Oliviers',
  address: '12 rue de la Paix',
  city: 'Casablanca',
  duesCalculationMode: 'FLAT_RATE',
  projectedBudget: null,
};

const addMutate = vi.fn();
const removeMutate = vi.fn();
const addMutateAsync = vi.fn().mockResolvedValue(undefined);
const removeMutateAsync = vi.fn().mockResolvedValue(undefined);

function setup(defined: { id: string; name: string }[], removeError: Error | null = null) {
  vi.mocked(useUnitTypeDefinitions).mockReturnValue({
    data: defined.map((unitType) => ({ ...unitType, propertyId: property.id })),
    isLoading: false,
    isError: false,
    error: null,
  } as never);
  vi.mocked(useAddUnitTypeDefinition).mockReturnValue({
    mutate: addMutate,
    mutateAsync: addMutateAsync,
    isPending: false,
    error: null,
  } as never);
  vi.mocked(useRemoveUnitTypeDefinition).mockReturnValue({
    mutate: removeMutate,
    mutateAsync: removeMutateAsync,
    isPending: false,
    error: removeError,
  } as never);

  render(
    <MemoryRouter initialEntries={['/configuration']}>
      <Routes>
        <Route path="/" element={<Outlet context={{ property }} />}>
          <Route path="configuration" element={<PropertyUnitTypesTab />} />
        </Route>
      </Routes>
    </MemoryRouter>,
  );
}

describe('PropertyUnitTypesTab', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  it('offre les mêmes types que le wizard, cochés selon ce que gère la copropriété', () => {
    setup([{ id: 'type-1', name: 'Appartement' }]);

    // La liste vient d'UNIT_TYPE_CHOICES, le paramètre applicatif que le wizard
    // d'inscription lit aussi.
    expect(screen.getByRole('checkbox', { name: 'Appartement' })).toBeChecked();
    expect(screen.getByRole('checkbox', { name: 'Parking' })).not.toBeChecked();
    expect(screen.getByRole('checkbox', { name: 'Box' })).not.toBeChecked();
    expect(screen.getByRole('checkbox', { name: 'Bureau' })).not.toBeChecked();
  });

  it('ne porte plus ni prix ni mode de calcul', () => {
    // Ils ont rejoint la configuration des échéances, seul endroit où ils servent.
    setup([{ id: 'type-1', name: 'Appartement' }]);

    expect(screen.queryByText(/Mode de gestion des appels de fonds/)).not.toBeInTheDocument();
    expect(screen.queryByText(/Budget prévisionnel/)).not.toBeInTheDocument();
    expect(screen.queryByLabelText(/prix/i)).not.toBeInTheDocument();
  });

  it('n’écrit rien tant que « Enregistrer » n’est pas cliqué', async () => {
    const user = userEvent.setup();
    setup([]);

    await user.click(screen.getByRole('checkbox', { name: 'Parking' }));

    expect(screen.getByRole('checkbox', { name: 'Parking' })).toBeChecked();
    expect(addMutateAsync).not.toHaveBeenCalled();
    expect(screen.getByText('Modifications non enregistrées.')).toBeInTheDocument();
  });

  it('enregistre les ajouts et les retraits en une fois', async () => {
    const user = userEvent.setup();
    setup([{ id: 'type-box', name: 'Box' }]);

    await user.click(screen.getByRole('checkbox', { name: 'Parking' }));
    await user.click(screen.getByRole('checkbox', { name: 'Box' }));
    await user.click(screen.getByRole('button', { name: 'Enregistrer' }));

    await waitFor(() => expect(addMutateAsync).toHaveBeenCalledWith('Parking'));
    // Le retrait passe avant l'ajout : il peut échouer (type porté par des
    // lots), et mieux vaut s'arrêter avant d'avoir écrit quoi que ce soit.
    expect(removeMutateAsync).toHaveBeenCalledWith('type-box');
  });

  it('garde le bouton inerte tant que rien n’a bougé', () => {
    setup([{ id: 'type-1', name: 'Appartement' }]);

    expect(screen.getByRole('button', { name: 'Enregistrer' })).toBeDisabled();
  });

  it('affiche le refus quand le type est porté par des lots', () => {
    // L'API refuse de supprimer un type utilisé : la case ne doit pas se
    // décocher en silence pour se recocher toute seule au rechargement.
    setup([{ id: 'type-appart', name: 'Appartement' }], new Error('Ce type de lot est utilisé par des lots.'));

    expect(screen.getByText('Ce type de lot est utilisé par des lots.')).toBeInTheDocument();
  });

  it('garde visible un type hérité d’une saisie libre', () => {
    // Sinon il deviendrait invisible et indéboulonnable.
    setup([{ id: 'type-cave', name: 'Cave' }]);

    expect(screen.getByRole('checkbox', { name: 'Cave' })).toBeChecked();
  });
});
