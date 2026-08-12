import { StyleSheet, Text } from 'react-native';
import type { NativeStackScreenProps } from '@react-navigation/native-stack';
import { AuthLayout } from '@/shared/layouts/AuthLayout';
import { Card } from '@/shared/components/Card/Card';
import { Button } from '@/shared/components/Button/Button';
import { RegisterPropertyAdminForm } from '@/features/identity/register/components/RegisterPropertyAdminForm';
import { useRegisterPropertyManagerAdmin } from '@/features/identity/register/hooks/useRegisterPropertyManagerAdmin';
import { getErrorMessage } from '@/shared/utils/getErrorMessage';
import { colors } from '@/shared/theme/colors';
import type { AuthStackParamList } from '@/app/navigation/AuthNavigator';
import type { RegisterPropertyAdminFormValues } from '@/features/identity/register/schemas/registerPropertyAdminSchema';

type Props = NativeStackScreenProps<AuthStackParamList, 'RegisterPropertyManagerAdmin'>;

export function RegisterPropertyManagerAdminScreen({ navigation }: Props) {
  const { mutate, isPending, isSuccess, error } = useRegisterPropertyManagerAdmin();

  function handleSubmit({ confirmPassword: _confirmPassword, ...values }: RegisterPropertyAdminFormValues) {
    mutate({ ...values, phone: values.phone || undefined });
  }

  if (isSuccess) {
    return (
      <AuthLayout>
        <Card style={styles.card}>
          <Text style={styles.title}>Vérifiez votre boîte mail</Text>
          <Text style={styles.body}>
            Votre compte et votre première copropriété ont été créés. Un email de confirmation vient de vous être
            envoyé. Touchez le lien qu'il contient pour activer votre compte.
          </Text>
          <Button variant="secondary" onPress={() => navigation.navigate('Login')} style={styles.linkButton}>
            Retour à la connexion
          </Button>
        </Card>
      </AuthLayout>
    );
  }

  return (
    <AuthLayout scrollable>
      <Card style={styles.card}>
        <Text style={styles.title}>Créer un compte cabinet de syndic</Text>
        <Text style={styles.body}>
          Vous gérez des copropriétés à titre professionnel. Vous pourrez en ajouter d'autres par la suite.
        </Text>
        <RegisterPropertyAdminForm
          onSubmit={handleSubmit}
          isSubmitting={isPending}
          errorMessage={error ? getErrorMessage(error) : undefined}
          submitLabel="Créer mon compte et ma première copropriété"
        />
        <Button variant="secondary" onPress={() => navigation.navigate('Login')} style={styles.linkButton}>
          Déjà un compte ? Se connecter
        </Button>
      </Card>
    </AuthLayout>
  );
}

const styles = StyleSheet.create({
  card: {
    gap: 8,
  },
  title: {
    fontSize: 18,
    fontWeight: '600',
    color: colors.gray[900],
  },
  body: {
    fontSize: 14,
    lineHeight: 20,
    color: colors.gray[600],
  },
  linkButton: {
    marginTop: 8,
  },
});
