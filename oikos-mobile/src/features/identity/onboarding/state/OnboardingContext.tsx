import { createContext, useCallback, useContext, useMemo, useState, type ReactNode } from 'react';
import { initialDraft, type OnboardingDraft } from '@/features/identity/onboarding/state/onboardingDraft';

interface OnboardingContextType {
  draft: OnboardingDraft;
  update: (patch: Partial<OnboardingDraft>) => void;
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
 * In-memory draft, like oikos-web's: an app kill mid-wizard loses the
 * configuration steps, while the account (step 1) and the property (step 2)
 * are already persisted by the API. The password is not kept at all: it
 * leaves for the API on step 1, which creates the account.
 */
export function OnboardingProvider({ children }: { children: ReactNode }) {
  const [draft, setDraft] = useState<OnboardingDraft>(() => initialDraft());

  const update = useCallback((patch: Partial<OnboardingDraft>) => {
    setDraft((previous) => ({ ...previous, ...patch }));
  }, []);

  const reset = useCallback(() => {
    setDraft(initialDraft());
  }, []);

  const value = useMemo(() => ({ draft, update, reset }), [draft, update, reset]);

  return <OnboardingContext.Provider value={value}>{children}</OnboardingContext.Provider>;
}
