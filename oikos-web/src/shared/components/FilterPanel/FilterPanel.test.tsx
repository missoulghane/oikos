import { useState } from 'react';
import { describe, expect, it, vi } from 'vitest';
import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { FilterPanel } from '@/shared/components/FilterPanel/FilterPanel';

function renderPanel(activeCount = 0, onClear = vi.fn()) {
  render(
    <FilterPanel activeCount={activeCount} onClear={onClear}>
      <label htmlFor="statut">Statut</label>
      <input id="statut" />
    </FilterPanel>,
  );
  return { onClear };
}

describe('FilterPanel', () => {
  it('starts collapsed so the list is what the reader meets first on a phone', () => {
    renderPanel();

    expect(screen.getByRole('button', { name: /Filtres/ })).toHaveAttribute('aria-expanded', 'false');
  });

  it('expands and collapses the fields from the toggle', async () => {
    renderPanel();
    const toggle = screen.getByRole('button', { name: /Filtres/ });

    await userEvent.click(toggle);
    expect(toggle).toHaveAttribute('aria-expanded', 'true');

    await userEvent.click(toggle);
    expect(toggle).toHaveAttribute('aria-expanded', 'false');
  });

  it('points the toggle at the panel it controls', () => {
    renderPanel();
    const toggle = screen.getByRole('button', { name: /Filtres/ });
    const controlledId = toggle.getAttribute('aria-controls');

    expect(controlledId).toBeTruthy();
    expect(document.getElementById(controlledId as string)).toContainElement(screen.getByLabelText('Statut'));
  });

  it('offers no reset action while no filter is active', () => {
    renderPanel(0);

    expect(screen.queryByRole('button', { name: /Réinitialiser/ })).not.toBeInTheDocument();
  });

  it('shows how many filters are active and resets them in one click', async () => {
    const { onClear } = renderPanel(2);

    expect(screen.getByRole('button', { name: /Filtres/ })).toHaveTextContent('2');

    await userEvent.click(screen.getByRole('button', { name: /Réinitialiser/ }));
    expect(onClear).toHaveBeenCalledOnce();
  });

  it('keeps the fields mounted while collapsed, so their values survive a toggle', () => {
    renderPanel();

    expect(screen.getByLabelText('Statut')).toBeInTheDocument();
  });

  // A folded panel hides the fields, so the toggle itself has to carry the
  // "this list is filtered" signal - otherwise a filtered list looks untouched.
  it('marks the toggle as active only once a filter is applied', () => {
    const { rerender } = render(
      <FilterPanel activeCount={0} onClear={vi.fn()}>
        <span />
      </FilterPanel>,
    );
    expect(screen.getByRole('button', { name: /Filtres/ }).className).not.toMatch(/brand/);

    rerender(
      <FilterPanel activeCount={1} onClear={vi.fn()}>
        <span />
      </FilterPanel>,
    );
    expect(screen.getByRole('button', { name: /Filtres/ }).className).toMatch(/brand/);
  });

  // Both labels are display:none below `sm` so the toolbar fits one row on a
  // phone; the accessible name has to come from aria-label, not that text.
  it('names both buttons independently of their visible label', () => {
    renderPanel(1);

    expect(screen.getByRole('button', { name: 'Filtres' })).toBeInTheDocument();
    expect(screen.getByRole('button', { name: 'Réinitialiser les filtres' })).toBeInTheDocument();
  });

  it('renders no search box unless one is configured', () => {
    renderPanel();

    expect(screen.queryByRole('searchbox')).not.toBeInTheDocument();
  });

  it('reports what is typed in the search box', async () => {
    // Stateful wrapper: the box is controlled, so without a parent feeding the
    // value back each keystroke would replace the previous one.
    function Harness() {
      const [value, setValue] = useState('');
      return (
        <FilterPanel activeCount={0} onClear={vi.fn()} search={{ value, onChange: setValue }}>
          <span />
        </FilterPanel>
      );
    }
    render(<Harness />);

    await userEvent.type(screen.getByRole('searchbox'), 'A1');

    expect(screen.getByRole('searchbox')).toHaveValue('A1');
  });

  it('keeps the search box reachable while the fields are folded away', () => {
    render(
      <FilterPanel activeCount={0} onClear={vi.fn()} search={{ value: '', onChange: vi.fn() }}>
        <span />
      </FilterPanel>,
    );

    expect(screen.getByRole('searchbox')).toBeInTheDocument();
    expect(screen.getByRole('button', { name: /Filtres/ })).toHaveAttribute('aria-expanded', 'false');
  });
});
