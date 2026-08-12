import { createContext, useCallback, useContext, useMemo, useState, type ReactNode } from 'react';
import { initialDraft, type OnboardingDraft } from '@/features/identity/onboarding/state/onboardingDraft';

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

/**
 * oikos-web persists `draft` to localStorage so an interrupted wizard survives
 * a page reload (never `password` - a plaintext password surviving in
 * localStorage would outlive the session). This port keeps the draft
 * in-memory only for now (no AsyncStorage dependency added this pass) - an
 * app kill mid-wizard loses progress on mobile, unlike on web. Worth adding
 * if this turns out to matter in practice; see PLAN.md.
 */
export function OnboardingProvider({ children }: { children: ReactNode }) {
  const [draft, setDraft] = useState<OnboardingDraft>(() => initialDraft());
  const [password, setPassword] = useState('');

  const update = useCallback((patch: Partial<OnboardingDraft>) => {
    setDraft((previous) => ({ ...previous, ...patch }));
  }, []);

  const reset = useCallback(() => {
    setPassword('');
    setDraft(initialDraft());
  }, []);

  const value = useMemo(
    () => ({ draft, update, password, setPassword, reset }),
    [draft, update, password, reset],
  );

  return <OnboardingContext.Provider value={value}>{children}</OnboardingContext.Provider>;
}
