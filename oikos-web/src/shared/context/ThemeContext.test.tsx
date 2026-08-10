import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest';
import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { ThemeProvider, useTheme } from '@/shared/context/ThemeContext';
import { ThemeToggleButton } from '@/shared/components/ThemeToggleButton/ThemeToggleButton';
import { THEME_MODE_STORAGE_KEY } from '@/config/theme';

function ModeProbe() {
  const { mode } = useTheme();
  return <span data-testid="mode">{mode}</span>;
}

function renderWithProvider() {
  render(
    <ThemeProvider>
      <ThemeToggleButton />
      <ModeProbe />
    </ThemeProvider>,
  );
}

/**
 * jsdom ships no matchMedia; resolveInitialMode() calls it optionally, so the
 * absence of this stub is itself a supported case (it means "no OS preference"
 * and falls back to light).
 */
function stubPrefersDark(prefersDark: boolean) {
  vi.stubGlobal(
    'matchMedia',
    vi.fn().mockReturnValue({
      matches: prefersDark,
      addEventListener: vi.fn(),
      removeEventListener: vi.fn(),
    }),
  );
}

describe('ThemeProvider', () => {
  beforeEach(() => {
    window.localStorage.clear();
    document.documentElement.classList.remove('dark');
    document.documentElement.removeAttribute('data-theme');
  });

  afterEach(() => {
    vi.unstubAllGlobals();
  });

  it('starts in light mode and adds no dark class when the OS has no dark preference', () => {
    stubPrefersDark(false);
    renderWithProvider();

    expect(screen.getByTestId('mode')).toHaveTextContent('light');
    expect(document.documentElement.classList.contains('dark')).toBe(false);
  });

  it('seeds the first visit from the OS preference', () => {
    stubPrefersDark(true);
    renderWithProvider();

    expect(screen.getByTestId('mode')).toHaveTextContent('dark');
    expect(document.documentElement.classList.contains('dark')).toBe(true);
  });

  it('prefers the stored choice over the OS preference', () => {
    stubPrefersDark(true);
    window.localStorage.setItem(THEME_MODE_STORAGE_KEY, 'light');
    renderWithProvider();

    expect(screen.getByTestId('mode')).toHaveTextContent('light');
    expect(document.documentElement.classList.contains('dark')).toBe(false);
  });

  it('toggles the dark class and persists the choice when the user clicks the switch', async () => {
    stubPrefersDark(false);
    const user = userEvent.setup();
    renderWithProvider();

    await user.click(screen.getByRole('button', { name: 'Passer en thème sombre' }));

    expect(document.documentElement.classList.contains('dark')).toBe(true);
    expect(document.documentElement.style.colorScheme).toBe('dark');
    expect(window.localStorage.getItem(THEME_MODE_STORAGE_KEY)).toBe('dark');

    await user.click(screen.getByRole('button', { name: 'Passer en thème clair' }));

    expect(document.documentElement.classList.contains('dark')).toBe(false);
    expect(window.localStorage.getItem(THEME_MODE_STORAGE_KEY)).toBe('light');
  });

  it('stamps no data-theme for the default blue palette (it is the one baked into @theme)', () => {
    stubPrefersDark(false);
    renderWithProvider();

    expect(document.documentElement.hasAttribute('data-theme')).toBe(false);
  });

  it('stamps the configured palette on <html> when it is not the default one', async () => {
    vi.resetModules();
    vi.doMock('@/config/theme', async () => {
      const actual = await vi.importActual<typeof import('@/config/theme')>('@/config/theme');
      return { ...actual, THEME_COLOR: 'emerald' };
    });
    const { ThemeProvider: EmeraldProvider } = await import('@/shared/context/ThemeContext');
    stubPrefersDark(false);

    render(
      <EmeraldProvider>
        <span>contenu</span>
      </EmeraldProvider>,
    );

    expect(document.documentElement.getAttribute('data-theme')).toBe('emerald');
    vi.doUnmock('@/config/theme');
    vi.resetModules();
  });
});
