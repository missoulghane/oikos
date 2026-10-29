import { vi } from 'vitest';
import '@testing-library/jest-dom/vitest';
import { configure } from '@testing-library/react';

// Default findBy*/waitFor timeout (1000ms) is tuned for a dev laptop; CI
// runners (shared vCPU, cold cache) can be slow enough to exceed it on
// otherwise-correct async assertions (observed in GitHub Actions on
// OnboardingWizard.test.tsx, never locally).
configure({ asyncUtilTimeout: 8000 });

// Les catalogues du produit (sièges du conseil, types de lot) viennent de l'API
// sur chaque écran qui les propose. Mockés ici une fois pour toute la suite, avec
// le contenu semé en base : un test qui veut un autre catalogue re-mocke le module.
vi.mock('@/shared/api/getBoardRoles', async () => {
  const { BOARD_ROLES_FIXTURE } = await import('./referenceCatalogs');
  return { getBoardRoles: vi.fn().mockResolvedValue(BOARD_ROLES_FIXTURE) };
});
vi.mock('@/shared/api/getUnitTypeChoices', async () => {
  const { UNIT_TYPE_CHOICES_FIXTURE } = await import('./referenceCatalogs');
  return { getUnitTypeChoices: vi.fn().mockResolvedValue(UNIT_TYPE_CHOICES_FIXTURE) };
});
