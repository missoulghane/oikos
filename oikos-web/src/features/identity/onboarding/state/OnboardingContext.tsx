import { createContext, useCallback, useContext, useMemo, useState, type ReactNode } from 'react';
import { initialDraft, type OnboardingDraft } from '@/features/identity/onboarding/state/onboardingDraft';

interface OnboardingContextType {
  draft: OnboardingDraft;
  update: (patch: Partial<OnboardingDraft>) => void;
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
 * Le brouillon vit en mémoire, et nulle part ailleurs : fermer l'onglet ou
 * recharger la page repart de l'étape 1.
 *
 * <p>Il était persisté dans le localStorage pour permettre de reprendre un
 * wizard interrompu. La reprise se payait cher : le brouillon survivait à la
 * fin du tunnel avec l'identifiant de la copropriété créée et son jeton, une
 * inscription relancée depuis le même navigateur repartait silencieusement sur
 * l'ancienne copropriété, et un brouillon écrit par une version antérieure du
 * wizard devait être recollé champ par champ à chaque déploiement sous peine
 * d'écran blanc. Le compte et la copropriété, eux, naissent à l'étape 2 et sont
 * bien persistés côté API : ce qu'un rechargement fait perdre, ce sont les
 * saisies des étapes de configuration.
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
