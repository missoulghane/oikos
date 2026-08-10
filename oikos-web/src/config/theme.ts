/**
 * Accent palettes available to the app. Each name must have a matching
 * `:root[data-theme='<name>']` block in src/index.css redefining the
 * --color-brand-* scale - except 'blue', which is the default already declared
 * in the `@theme` block (hence no data-theme attribute is stamped for it).
 */
export const THEME_COLORS = ['blue', 'emerald', 'violet', 'amber'] as const;

export type ThemeColor = (typeof THEME_COLORS)[number];

/**
 * The app's accent colour. Deliberately a build-time constant and NOT a user
 * setting: end users only ever choose between light and dark (see
 * ThemeProvider). Change the value here to re-skin the whole app.
 */
export const THEME_COLOR: ThemeColor = 'blue';

/** localStorage key holding the user's light/dark choice. */
export const THEME_MODE_STORAGE_KEY = 'oikos-theme-mode';

export type ThemeMode = 'light' | 'dark';
