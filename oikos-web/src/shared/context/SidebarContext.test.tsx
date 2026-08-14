import { describe, expect, it } from 'vitest';
import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { SidebarProvider, useSidebar } from '@/shared/context/SidebarContext';

const seenCloseFns = new Set<() => void>();

function Consumer() {
  const { isMobileOpen, toggleMobileSidebar, closeMobileSidebar } = useSidebar();
  seenCloseFns.add(closeMobileSidebar);

  return (
    <div>
      <span>{isMobileOpen ? 'ouvert' : 'fermé'}</span>
      <button type="button" onClick={toggleMobileSidebar}>
        basculer
      </button>
      <button type="button" onClick={closeMobileSidebar}>
        fermer
      </button>
    </div>
  );
}

describe('SidebarProvider', () => {
  it('opens and closes the mobile drawer', async () => {
    render(
      <SidebarProvider>
        <Consumer />
      </SidebarProvider>,
    );
    expect(screen.getByText('fermé')).toBeInTheDocument();

    await userEvent.click(screen.getByRole('button', { name: 'basculer' }));
    expect(screen.getByText('ouvert')).toBeInTheDocument();

    await userEvent.click(screen.getByRole('button', { name: 'fermer' }));
    expect(screen.getByText('fermé')).toBeInTheDocument();
  });

  it('closes without re-opening, unlike the toggle', async () => {
    render(
      <SidebarProvider>
        <Consumer />
      </SidebarProvider>,
    );

    await userEvent.click(screen.getByRole('button', { name: 'fermer' }));
    await userEvent.click(screen.getByRole('button', { name: 'fermer' }));

    expect(screen.getByText('fermé')).toBeInTheDocument();
  });

  // AppSidebar closes the drawer from an effect keyed on the route. If this
  // identity changed per render, that effect would re-run continuously and the
  // drawer could never stay open at all.
  it('keeps a stable closeMobileSidebar identity across re-renders', async () => {
    seenCloseFns.clear();
    render(
      <SidebarProvider>
        <Consumer />
      </SidebarProvider>,
    );

    await userEvent.click(screen.getByRole('button', { name: 'basculer' }));
    await userEvent.click(screen.getByRole('button', { name: 'basculer' }));

    expect(seenCloseFns.size).toBe(1);
  });
});
