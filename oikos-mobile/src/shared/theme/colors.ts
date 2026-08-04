/**
 * Same TailAdmin palette as oikos-web (see oikos-web/src/index.css) so the two
 * clients read as the same product, ported to plain hex for RN StyleSheet.
 */
export const colors = {
  brand: {
    50: '#ecf3ff',
    300: '#9cb9ff',
    500: '#465fff',
    600: '#3641f5',
  },
  gray: {
    50: '#f9fafb',
    100: '#f2f4f7',
    200: '#e4e7ec',
    300: '#d0d5dd',
    400: '#98a2b3',
    500: '#667085',
    600: '#475467',
    700: '#344054',
    800: '#1d2939',
    900: '#101828',
  },
  success: {
    50: '#ecfdf3',
    500: '#12b76a',
  },
  error: {
    50: '#fef3f2',
    300: '#fda29b',
    500: '#f04438',
  },
  warning: {
    50: '#fffaeb',
    500: '#f79009',
  },
  white: '#ffffff',
} as const;
