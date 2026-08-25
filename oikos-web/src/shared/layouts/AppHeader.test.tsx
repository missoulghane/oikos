import { beforeEach, describe, expect, it, vi } from 'vitest';
import { render, screen } from '@testing-library/react';
import { MemoryRouter } from 'react-router-dom';

vi.mock('@/shared/context/SidebarContext', () => ({
  useSidebar: () => ({
    isExpanded: true,
    isMobileOpen: false,
    isHovered: false,
    toggleSidebar: vi.fn(),
    toggleMobileSidebar: vi.fn(),
    setIsHovered: vi.fn(),
  }),
}));
// Rendus identifiables, pas de simples <div> : c'est ce qui doit rester quand
// les cloches disparaissent, et une assertion ne peut pas le vérifier sur du vide.
vi.mock('@/shared/layouts/UserDropdown', () => ({ UserDropdown: () => <div>Menu du profil</div> }));
vi.mock('@/shared/components/ThemeToggleButton/ThemeToggleButton', () => ({
  ThemeToggleButton: () => <div>Thème clair/sombre</div>,
}));
vi.mock('@/shared/components/SpaceSwitcher/SpaceSwitcher', () => ({ SpaceSwitcher: () => <div /> }));
vi.mock('@/features/messaging', () => ({
  NotificationBell: () => <button type="button" aria-label="Notifications de messagerie" />,
}));
vi.mock('@/features/notifications', () => ({
  NotificationsBell: () => <button type="button" aria-label="Notifications" />,
}));
// Mutable : le même compte, avant et après la validation de sa demande
// d'adhésion. La factory ne le lit qu'au rendu.
let noPropertyAccess = false;
vi.mock('@/features/identity/me', () => ({
  useCurrentUser: () => ({ data: { id: 'u1' } }),
  hasNoPropertyAccess: () => noPropertyAccess,
}));

const { AppHeader } = await import('@/shared/layouts/AppHeader');

function renderHeader() {
  return render(
    <MemoryRouter>
      <AppHeader />
    </MemoryRouter>,
  );
}

describe('AppHeader', () => {
  beforeEach(() => {
    noPropertyAccess = false;
  });

  // The sidebar "Notifications" entry was removed on purpose; this bell is now
  // the only way into /notifications, so it must never be dropped as dead code.
  it('keeps the notifications bell as the entry point to the notification centre', () => {
    renderHeader();

    expect(screen.getByRole('button', { name: 'Notifications' })).toBeInTheDocument();
  });

  it('keeps the messaging bell alongside it', () => {
    renderHeader();

    expect(screen.getByRole('button', { name: 'Notifications de messagerie' })).toBeInTheDocument();
  });

  /**
   * Le compte dont la demande d'adhésion attend le syndic : il n'appartient à
   * aucune copropriété, donc ni message ni notification ne peut lui parvenir.
   * Les deux cloches suivent les entrées de menu correspondantes, retirées
   * pour la même raison (voir AppSidebar).
   */
  describe('aucune affectation valide', () => {
    beforeEach(() => {
      noPropertyAccess = true;
    });

    it('retire les deux cloches', () => {
      renderHeader();

      expect(screen.queryByRole('button', { name: 'Notifications' })).not.toBeInTheDocument();
      expect(screen.queryByRole('button', { name: 'Notifications de messagerie' })).not.toBeInTheDocument();
    });

    // Le profil reste atteignable : c'est la décision prise avec la sidebar,
    // et le seul endroit où ce compte peut encore agir sur lui-même.
    it('laisse le thème et le menu du profil en place', () => {
      renderHeader();

      expect(screen.getByText('Menu du profil')).toBeInTheDocument();
      expect(screen.getByText('Thème clair/sombre')).toBeInTheDocument();
    });
  });
});
