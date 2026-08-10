import { createContext, useCallback, useContext, useEffect, useState, type ReactNode } from 'react';
import { THEME_COLOR, THEME_MODE_STORAGE_KEY, type ThemeMode } from '@/config/theme';

type ThemeContextType = {
  mode: ThemeMode;
  toggleMode: () => void;
  setMode: (mode: ThemeMode) => void;
};

const ThemeContext = createContext<ThemeContextType | undefined>(undefined);

export function useTheme() {
  const context = useContext(ThemeContext);
  if (!context) {
    throw new Error('useTheme must be used within a ThemeProvider');
  }
  return context;
}

/**
 * The stored choice wins; the OS preference only seeds the very first visit.
 * Exported because the anti-flash script in index.html must resolve the exact
 * same mode before React mounts - if the two ever disagree the page repaints.
 */
export function resolveInitialMode(): ThemeMode {
  const stored = window.localStorage.getItem(THEME_MODE_STORAGE_KEY);
  if (stored === 'light' || stored === 'dark') {
    return stored;
  }
  return window.matchMedia?.('(prefers-color-scheme: dark)').matches ? 'dark' : 'light';
}

function applyMode(mode: ThemeMode) {
  document.documentElement.classList.toggle('dark', mode === 'dark');
  // Lets the browser paint native widgets (scrollbars, form controls, the
  // date picker popup) in the matching mode - CSS alone cannot do that.
  document.documentElement.style.colorScheme = mode;
}

export function ThemeProvider({ children }: { children: ReactNode }) {
  // Read synchronously rather than in an effect: the class is already on <html>
  // (index.html script), so starting from 'light' would make the toggle button
  // render the wrong icon for a frame and immediately flip the page back.
  const [mode, setModeState] = useState<ThemeMode>(() => resolveInitialMode());

  useEffect(() => {
    applyMode(mode);
    window.localStorage.setItem(THEME_MODE_STORAGE_KEY, mode);
  }, [mode]);

  // The accent palette is a code-level setting (src/config/theme.ts), so it is
  // stamped once and never changes at runtime. 'blue' is the palette baked into
  // the @theme block, so it needs no attribute at all.
  useEffect(() => {
    if (THEME_COLOR === 'blue') {
      document.documentElement.removeAttribute('data-theme');
    } else {
      document.documentElement.setAttribute('data-theme', THEME_COLOR);
    }
  }, []);

  const setMode = useCallback((next: ThemeMode) => setModeState(next), []);
  const toggleMode = useCallback(
    () => setModeState((previous) => (previous === 'dark' ? 'light' : 'dark')),
    [],
  );

  return <ThemeContext.Provider value={{ mode, toggleMode, setMode }}>{children}</ThemeContext.Provider>;
}
