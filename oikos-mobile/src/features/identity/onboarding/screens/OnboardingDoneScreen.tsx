import { StyleSheet, Text, View } from 'react-native';
import type { CompositeScreenProps } from '@react-navigation/native';
import type { NativeStackScreenProps } from '@react-navigation/native-stack';
import { AuthLayout } from '@/shared/layouts/AuthLayout';
import { Card } from '@/shared/components/Card/Card';
import { Button } from '@/shared/components/Button/Button';
import { useOnboarding } from '@/features/identity/onboarding/state/OnboardingContext';
import { colors } from '@/shared/theme/colors';
import type { OnboardingStackParamList } from '@/app/navigation/OnboardingNavigator';
import type { AuthStackParamList } from '@/app/navigation/AuthNavigator';

type Props = CompositeScreenProps<
  NativeStackScreenProps<OnboardingStackParamList, 'Done'>,
  NativeStackScreenProps<AuthStackParamList>
>;

export function OnboardingDoneScreen({ navigation }: Props) {
  const { draft } = useOnboarding();

  return (
    <AuthLayout>
      <Card style={styles.card}>
        <View style={styles.iconCircle}>
          <Text style={styles.icon}>✓</Text>
        </View>
        <Text style={styles.title}>Votre copropriété est créée !</Text>
        <Text style={styles.body}>
          Votre espace de gestion est prêt. Vous pourrez ajouter les copropriétaires et compléter les informations
          de vos lots depuis l'application web en attendant leur portage mobile.
        </Text>

        {/* Le compte n'est pas encore vérifié : sans ce rappel, le bouton
            ci-dessous renverrait vers un écran de connexion inexplicable. */}
        <Text style={styles.reminder}>
          Un email de confirmation vous a été envoyé à <Text style={styles.reminderEmail}>{draft.account.email}</Text>.
          Activez votre compte pour accéder à votre espace.
        </Text>

        <Button onPress={() => navigation.navigate('Login')} style={styles.button}>
          Accéder à ma copropriété
        </Button>
      </Card>
    </AuthLayout>
  );
}

const styles = StyleSheet.create({
  card: {
    alignItems: 'center',
    gap: 8,
  },
  iconCircle: {
    width: 48,
    height: 48,
    borderRadius: 24,
    backgroundColor: colors.success[50],
    alignItems: 'center',
    justifyContent: 'center',
    marginBottom: 8,
  },
  icon: {
    fontSize: 22,
    fontWeight: '700',
    color: colors.success[500],
  },
  title: {
    fontSize: 18,
    fontWeight: '600',
    color: colors.gray[900],
    textAlign: 'center',
  },
  body: {
    fontSize: 14,
    color: colors.gray[600],
    textAlign: 'center',
  },
  reminder: {
    marginTop: 8,
    borderRadius: 8,
    backgroundColor: colors.gray[50],
    padding: 12,
    fontSize: 14,
    color: colors.gray[600],
    textAlign: 'center',
  },
  reminderEmail: {
    fontWeight: '600',
  },
  button: {
    marginTop: 16,
    alignSelf: 'stretch',
  },
});
