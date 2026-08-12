import type { PropsWithChildren } from 'react';
import { StyleSheet, Text, View } from 'react-native';
import { Card } from '@/shared/components/Card/Card';
import { Button } from '@/shared/components/Button/Button';
import { colors } from '@/shared/theme/colors';
import { ONBOARDING_STEPS, stepIndexOf, type OnboardingStepScreen } from '@/features/identity/onboarding/constants/steps';

interface WizardShellProps {
  step: OnboardingStepScreen;
  title: string;
  subtitle: string;
  /** Set when there's a previous step to go back to (first step has none). */
  onBack?: () => void;
}

export function WizardShell({ step, title, subtitle, onBack, children }: PropsWithChildren<WizardShellProps>) {
  const currentIndex = stepIndexOf(step);
  const progress = ((currentIndex + 1) / ONBOARDING_STEPS.length) * 100;

  return (
    <Card style={styles.card}>
      <View style={styles.progressHeader}>
        <View style={styles.progressLabels}>
          <Text style={styles.progressText}>
            Étape {currentIndex + 1} sur {ONBOARDING_STEPS.length}
          </Text>
          <Text style={styles.progressText}>{ONBOARDING_STEPS[currentIndex]?.label}</Text>
        </View>
        <View
          accessibilityRole="progressbar"
          accessibilityValue={{ min: 1, max: ONBOARDING_STEPS.length, now: currentIndex + 1 }}
          style={styles.progressTrack}
        >
          <View style={[styles.progressFill, { width: `${progress}%` }]} />
        </View>
      </View>

      <Text style={styles.title}>{title}</Text>
      <Text style={styles.subtitle}>{subtitle}</Text>

      {children}

      {onBack && (
        <Button variant="secondary" onPress={onBack} style={styles.backButton}>
          ← Revenir à l'étape précédente
        </Button>
      )}
    </Card>
  );
}

const styles = StyleSheet.create({
  card: {
    gap: 4,
  },
  progressHeader: {
    gap: 8,
    marginBottom: 8,
  },
  progressLabels: {
    flexDirection: 'row',
    justifyContent: 'space-between',
  },
  progressText: {
    fontSize: 12,
    fontWeight: '500',
    color: colors.gray[500],
  },
  progressTrack: {
    height: 6,
    borderRadius: 3,
    backgroundColor: colors.gray[100],
    overflow: 'hidden',
  },
  progressFill: {
    height: '100%',
    borderRadius: 3,
    backgroundColor: colors.brand[500],
  },
  title: {
    fontSize: 18,
    fontWeight: '600',
    color: colors.gray[900],
  },
  subtitle: {
    marginTop: 4,
    marginBottom: 16,
    fontSize: 14,
    color: colors.gray[600],
  },
  backButton: {
    marginTop: 16,
  },
});
