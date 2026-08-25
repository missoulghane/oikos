import { describe, expect, it, vi } from 'vitest';
import { render, screen } from '@testing-library/react';
import { MemoryRouter, Outlet, Route, Routes } from 'react-router-dom';
import { PropertyGeneralInfoTab } from '@/features/property-mngt/properties/pages/PropertyGeneralInfoTab';
import type { Property } from '@/features/property-mngt/properties/types/property.types';

// Le bloc du conseil syndical lit ses propres données (react-query) et a ses
// tests à lui : ici on vérifie seulement qu'il est bien sur cette page, la
// fusion des deux onglets tenant à cela.
vi.mock('@/features/property-mngt/board-members', () => ({
  BoardSection: () => <section>Conseil syndical (bloc)</section>,
}));

// Même raison pour le lien public d'adhésion, qui a rejoint cette page : il
// lit le sien via react-query et se teste chez lui.
vi.mock('@/features/property-mngt/invitations', () => ({
  PublicInvitationSection: () => <section>Lien public (bloc)</section>,
}));

const property: Property = {
  id: 'p-1',
  name: 'Résidence Les Oliviers',
  address: '12 rue de la Paix',
  city: 'Casablanca',
  duesCalculationMode: 'FLAT_RATE',
  projectedBudget: null,
};

/**
 * L'onglet lit sa copropriété via useOutletContext : la route parente est donc
 * montée pour de vrai, comme PropertyDetailLayout le fait en application.
 */
function renderTab(override: Partial<Property> = {}) {
  render(
    <MemoryRouter initialEntries={['/general']}>
      <Routes>
        <Route path="/" element={<Outlet context={{ property: { ...property, ...override } }} />}>
          <Route path="general" element={<PropertyGeneralInfoTab />} />
        </Route>
      </Routes>
    </MemoryRouter>,
  );
}

describe('PropertyGeneralInfoTab', () => {
  it('affiche la ville à côté de l’adresse', () => {
    renderTab();

    expect(screen.getByText('Adresse')).toBeInTheDocument();
    expect(screen.getByText('12 rue de la Paix')).toBeInTheDocument();
    expect(screen.getByText('Ville')).toBeInTheDocument();
    expect(screen.getByText('Casablanca')).toBeInTheDocument();
  });

  it('garde la ligne « Ville » sur une copropriété qui n’en a pas', () => {
    // Les copropriétés créées avant l'existence du champ : faire disparaître la
    // ligne laisserait croire que la ville n'est pas demandée.
    renderTab({ city: null });

    expect(screen.getByText('Ville')).toBeInTheDocument();
    expect(screen.getByText('—')).toBeInTheDocument();
  });

  it('porte le bloc du conseil syndical, sous les informations', () => {
    renderTab();

    expect(screen.getByText('Conseil syndical (bloc)')).toBeInTheDocument();
  });

  // Le lien public appartient à la copropriété au même titre que son nom : il
  // vivait dans un onglet « Invitations » où personne n'allait le chercher.
  it("porte le lien public d'adhésion", () => {
    renderTab();

    expect(screen.getByText('Lien public (bloc)')).toBeInTheDocument();
  });
});
