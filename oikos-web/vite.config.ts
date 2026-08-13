import { defineConfig } from 'vitest/config';
import react from '@vitejs/plugin-react';
import tailwindcss from '@tailwindcss/vite';
import svgr from 'vite-plugin-svgr';
import path from 'node:path';

// https://vite.dev/config/
export default defineConfig({
  plugins: [
    react(),
    tailwindcss(),
    svgr({
      svgrOptions: {
        icon: true,
        exportType: 'named',
        namedExport: 'ReactComponent',
      },
    }),
  ],
  resolve: {
    alias: {
      '@': path.resolve(__dirname, './src'),
    },
  },
  server: {
    port: 5173,
    proxy: {
      // Avoids CORS setup for local dev: the browser talks to Vite on 5173,
      // Vite forwards /api requests server-side to the Spring Boot API.
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true,
      },
    },
  },
  test: {
    environment: 'jsdom',
    globals: true,
    setupFiles: ['./src/test/setup.ts'],
    // Node 22+'s own (experimental, disabled-by-default) global `localStorage`
    // shadows jsdom's working implementation once it exists as a global at
    // all, breaking every test that touches window.localStorage. Disabling
    // it lets jsdom's own polyfill through untouched.
    execArgv: ['--no-experimental-webstorage'],
    // Default (5000ms) is tight on a shared CI runner for tests that chain
    // several findBy*/waitFor steps (e.g. the onboarding wizard) — each
    // individual wait already gets more room via asyncUtilTimeout
    // (src/test/setup.ts), this covers their sum within one test.
    testTimeout: 20000,
  },
});
