import { createContext, useCallback, useContext, useEffect, useMemo, useState, type ReactNode } from 'react';
import { initialDraft, mergeStoredDraft, type OnboardingDraft } from '@/features/identity/onboarding/state/onboardingDraft';

const STORAGE_KEY = 'oikos-onboarding-draft';

interface OnboardingContextType {
  draft: OnboardingDraft;
  update: (patch: Partial<OnboardingDraft>) => void;
  /** Mot de passe gardé en mémoire uniquement - voir le commentaire de persistance. */
  password: string;
  setPassword: (password: string) => void;
  reset: () => void;
}

const OnboardingContext = createContext<OnboardingContextType | undefined>(undefined);

export function useOnboarding() {
  const context = useContext(OnboardingContext);
  if (!context) {
    throw new Error('useOnboarding must be used within an OnboardingProvider');
  }
  return context;
}

function readStoredDraft(): OnboardingDraft {
  try {
    const stored = window.localStorage.getItem(STORAGE_KEY);
    if (!stored) {
      return initialDraft();
    }
    // Fusion avec les valeurs par défaut : un brouillon écrit par une version
    // antérieure du wizard n'a pas forcément toutes les clés attendues.
    return mergeStoredDraft(JSON.parse(stored) as Partial<OnboardingDraft>);
  } catch {
    return initialDraft();
  }
}

export function OnboardingProvider({ children }: { children: ReactNode }) {
  const [draft, setDraft] = useState<OnboardingDraft>(() => readStoredDraft());
  // Jamais persisté : un mot de passe en clair dans le localStorage survivrait à
  // la session et serait lisible par n'importe quel script de la page. Reprendre
  // un wizard interrompu après l'étape 2 n'en a de toute façon pas besoin, le
  // compte étant déjà créé.
  const [password, setPassword] = useState('');

  useEffect(() => {
    try {
      window.localStorage.setItem(STORAGE_KEY, JSON.stringify(draft));
    } catch {
      // Stockage indisponible (navigation privée) : le wizard reste utilisable
      // d'une traite, seule la reprise après fermeture est perdue.
    }
  }, [draft]);

  const update = useCallback((patch: Partial<OnboardingDraft>) => {
    setDraft((previous) => ({ ...previous, ...patch }));
  }, []);

  const reset = useCallback(() => {
    setPassword('');
    setDraft(initialDraft());
    try {
      window.localStorage.removeItem(STORAGE_KEY);
    } catch {
      // idem
    }
  }, []);

  const value = useMemo(
    () => ({ draft, update, password, setPassword, reset }),
    [draft, update, password, reset],
  );

  return <OnboardingContext.Provider value={value}>{children}</OnboardingContext.Provider>;
}
