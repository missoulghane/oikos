import '@testing-library/jest-dom/vitest';
import { configure } from '@testing-library/react';

// Default findBy*/waitFor timeout (1000ms) is tuned for a dev laptop; CI
// runners (shared vCPU, cold cache) can be slow enough to exceed it on
// otherwise-correct async assertions (observed in GitHub Actions on
// OnboardingWizard.test.tsx, never locally).
configure({ asyncUtilTimeout: 5000 });
