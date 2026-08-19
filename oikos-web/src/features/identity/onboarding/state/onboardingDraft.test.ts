import { describe, expect, it } from 'vitest';
import { initialDraft, mergeStoredDraft } from '@/features/identity/onboarding/state/onboardingDraft';

describe('mergeStoredDraft', () => {
  it("complète un compte écrit avant l'ajout du téléphone", () => {
    // Le cas qui plantait l'étape 1 : `account` remplaçait l'objet par défaut en
    // entier, `phone` valait `undefined`, et PhoneField tombait au premier rendu.
    const legacy = { account: { fullName: 'Jane Doe', email: 'jane@example.com' } };

    expect(mergeStoredDraft(legacy as never).account).toEqual({
      fullName: 'Jane Doe',
      email: 'jane@example.com',
      phone: '',
    });
  });

  it('garde les valeurs par défaut des clés absentes', () => {
    const merged = mergeStoredDraft({ projectedBudget: '120000' });

    expect(merged.projectedBudget).toBe('120000');
    expect(merged.property).toEqual(initialDraft().property);
    expect(merged.buildings).toEqual(initialDraft().buildings);
  });

  it('écarte une collection corrompue plutôt que de la propager', () => {
    const merged = mergeStoredDraft({ buildings: null, selectedUnitTypes: 'Appartement' } as never);

    expect(merged.buildings).toEqual(initialDraft().buildings);
    expect(merged.selectedUnitTypes).toEqual(initialDraft().selectedUnitTypes);
  });

  it('rend un brouillon neuf quand il n’y a rien à relire', () => {
    expect(mergeStoredDraft(null)).toEqual(initialDraft());
    expect(mergeStoredDraft(undefined)).toEqual(initialDraft());
  });
});
