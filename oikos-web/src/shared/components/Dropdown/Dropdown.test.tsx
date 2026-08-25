import { describe, expect, it } from 'vitest';
import { useState } from 'react';
import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { Dropdown } from '@/shared/components/Dropdown/Dropdown';

/** Le même gabarit que la cloche des notifications, des messages et du profil. */
function Panel({ name }: { name: string }) {
  const [isOpen, setIsOpen] = useState(false);
  return (
    <div className="relative">
      <button type="button" className="dropdown-toggle" onClick={() => setIsOpen((value) => !value)}>
        {`Ouvrir ${name}`}
      </button>
      <Dropdown isOpen={isOpen} onClose={() => setIsOpen(false)}>
        <p>{`Contenu ${name}`}</p>
        <button type="button">{`Action ${name}`}</button>
      </Dropdown>
    </div>
  );
}

function renderHeader() {
  return render(
    <div>
      <Panel name="notifications" />
      <Panel name="messages" />
    </div>,
  );
}

describe('Dropdown', () => {
  // Le bug signalé : ouvrir les notifications puis cliquer directement sur
  // messages laissait les deux panneaux superposés.
  it('closes when another dropdown toggle is clicked', async () => {
    renderHeader();
    await userEvent.click(screen.getByRole('button', { name: 'Ouvrir notifications' }));
    expect(screen.getByText('Contenu notifications')).toBeInTheDocument();

    await userEvent.click(screen.getByRole('button', { name: 'Ouvrir messages' }));

    expect(screen.queryByText('Contenu notifications')).not.toBeInTheDocument();
    expect(screen.getByText('Contenu messages')).toBeInTheDocument();
  });

  it('closes when anything else on the page is clicked', async () => {
    renderHeader();
    await userEvent.click(screen.getByRole('button', { name: 'Ouvrir notifications' }));

    await userEvent.click(document.body);

    expect(screen.queryByText('Contenu notifications')).not.toBeInTheDocument();
  });

  // Sinon le panneau se refermerait sur le premier geste qu'on y fait.
  it('stays open while the click lands inside its own panel', async () => {
    renderHeader();
    await userEvent.click(screen.getByRole('button', { name: 'Ouvrir notifications' }));

    await userEvent.click(screen.getByRole('button', { name: 'Action notifications' }));

    expect(screen.getByText('Contenu notifications')).toBeInTheDocument();
  });

  // Le bouton porte lui-même la bascule : si le clic extérieur fermait aussi,
  // les deux se battraient et le panneau rouvrirait aussitôt.
  it('leaves its own toggle to close it, exactly once', async () => {
    renderHeader();
    await userEvent.click(screen.getByRole('button', { name: 'Ouvrir notifications' }));

    await userEvent.click(screen.getByRole('button', { name: 'Ouvrir notifications' }));

    expect(screen.queryByText('Contenu notifications')).not.toBeInTheDocument();
  });
});
