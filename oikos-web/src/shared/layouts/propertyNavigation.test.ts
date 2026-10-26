import { describe, expect, it } from 'vitest';
import { activePropertyMenuName } from '@/shared/layouts/propertyNavigation';

const base = '/property-mngt/properties/p-1';

describe('activePropertyMenuName', () => {
  it('names the menu entry of the current page, as the sidebar highlights it', () => {
    expect(activePropertyMenuName('p-1', `${base}/property/configuration`)).toBe('Configuration');
    expect(activePropertyMenuName('p-1', `${base}/property/documents`)).toBe('Documents');
    // The index entry does not swallow its siblings.
    expect(activePropertyMenuName('p-1', `${base}/property`)).toBe('Informations générales');
    expect(activePropertyMenuName('p-1', `${base}/installments/calls`)).toBe('Appels de fonds');
  });

  it('names a nested page after the menu it belongs to', () => {
    expect(activePropertyMenuName('p-1', `${base}/general-meetings/m-1/agenda`)).toBe('Assemblées');
    expect(activePropertyMenuName('p-1', `${base}/accounting/journal/e-1`)).toBe('Journal');
  });

  it('returns null outside the menus of this property', () => {
    expect(activePropertyMenuName('p-1', '/dashboard')).toBeNull();
    expect(activePropertyMenuName('p-1', '/property-mngt/properties/p-2/property')).toBeNull();
  });
});
