import type { PropsWithChildren } from 'react';
import { Link } from 'react-router-dom';
import { Card } from '@/shared/components/Card/Card';
import { ONBOARDING_STEPS, stepIndexOf, type OnboardingStepSlug } from '@/features/identity/onboarding/constants/steps';

interface WizardShellProps {
  step: OnboardingStepSlug;
  title: string;
  subtitle: string;
  /** Étape précédente, ou null sur la première (aucun retour à afficher). */
  backPath?: string | null;
}

export function WizardShell({
  step,
  title,
  subtitle,
  backPath,
  children,
}: PropsWithChildren<WizardShellProps>) {
  const currentIndex = stepIndexOf(step);
  const progress = ((currentIndex + 1) / ONBOARDING_STEPS.length) * 100;

  return (
    <Card className="w-full max-w-2xl">
      <div className="mb-6 flex flex-col gap-2">
        <div className="flex items-center justify-between text-xs font-medium text-gray-500 dark:text-gray-400">
          <span>
            Étape {currentIndex + 1} sur {ONBOARDING_STEPS.length}
          </span>
          <span>{ONBOARDING_STEPS[currentIndex]?.label}</span>
        </div>
        <div
          className="h-1.5 w-full overflow-hidden rounded-full bg-gray-100 dark:bg-white/[0.08]"
          role="progressbar"
          aria-valuenow={currentIndex + 1}
          aria-valuemin={1}
          aria-valuemax={ONBOARDING_STEPS.length}
          aria-label="Progression de la configuration"
        >
          <div className="h-full rounded-full bg-brand-500 transition-all duration-300" style={{ width: `${progress}%` }} />
        </div>
      </div>

      <h1 className="text-lg font-semibold text-gray-900 dark:text-white/90">{title}</h1>
      <p className="mt-1 mb-6 text-sm text-gray-600 dark:text-gray-400">{subtitle}</p>

      {children}

      {backPath && (
        <Link
          to={backPath}
          className="mt-4 inline-block text-sm font-medium text-gray-600 underline dark:text-gray-400"
        >
          ← Revenir à l'étape précédente
        </Link>
      )}
    </Card>
  );
}
