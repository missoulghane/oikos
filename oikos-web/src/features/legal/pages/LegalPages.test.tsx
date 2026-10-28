import type { ReactElement } from 'react';
import { render, screen } from '@testing-library/react';
import { MemoryRouter } from 'react-router-dom';
import { describe, expect, it } from 'vitest';
import { TermsOfUsePage } from '@/features/legal/pages/TermsOfUsePage';
import { TermsOfSalePage } from '@/features/legal/pages/TermsOfSalePage';

function renderPage(page: ReactElement) {
  return render(<MemoryRouter>{page}</MemoryRouter>);
}

describe('legal pages', () => {
  it('shows the terms of use, citing the data protection law and linking to the terms of sale', () => {
    renderPage(<TermsOfUsePage />);

    expect(screen.getByRole('heading', { level: 1, name: "Conditions générales d'utilisation" })).toBeInTheDocument();
    expect(screen.getByRole('heading', { name: '6. Protection des données à caractère personnel' })).toBeInTheDocument();
    expect(screen.getByRole('link', { name: 'Conditions générales de vente' })).toHaveAttribute('href', '/cgv');
  });

  it('shows the terms of sale, with the withdrawal right, and links back to the terms of use', () => {
    renderPage(<TermsOfSalePage />);

    expect(screen.getByRole('heading', { level: 1, name: 'Conditions générales de vente' })).toBeInTheDocument();
    expect(screen.getByRole('heading', { name: '8. Droit de rétractation' })).toBeInTheDocument();
    expect(screen.getByRole('link', { name: "Conditions générales d'utilisation" })).toHaveAttribute('href', '/cgu');
  });

  it('flags itself as provisional until the publisher details are filled in', () => {
    renderPage(<TermsOfUsePage />);

    expect(screen.getByText(/Version provisoire/)).toBeInTheDocument();
  });
});
