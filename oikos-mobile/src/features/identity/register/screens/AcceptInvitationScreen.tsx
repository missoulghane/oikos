import { StyleSheet, Text } from 'react-native';
import type { NativeStackScreenProps } from '@react-navigation/native-stack';
import { AuthLayout } from '@/shared/layouts/AuthLayout';
import { Card } from '@/shared/components/Card/Card';
import { Alert } from '@/shared/components/Alert/Alert';
import { Button } from '@/shared/components/Button/Button';
import { AcceptInvitationForm } from '@/features/identity/register/components/AcceptInvitationForm';
import { useAcceptInvitation } from '@/features/identity/register/hooks/useAcceptInvitation';
import { getErrorMessage } from '@/shared/utils/getErrorMessage';
import { colors } from '@/shared/theme/colors';
import type { AuthStackParamList } from '@/app/navigation/AuthNavigator';
import type { AcceptInvitationFormValues } from '@/features/identity/register/schemas/acceptInvitationSchema';

type Props = NativeStackScreenProps<AuthStackParamList, 'AcceptInvitation'>;

export function AcceptInvitationScreen({ route, navigation }: Props) {
  const { token } = route.params ?? {};
  const { mutate, isPending, isSuccess, error } = useAcceptInvitation();

  function handleSubmit(values: AcceptInvitationFormValues) {
    if (token) {
      mutate({ token, password: values.password === '' ? undefined : values.password });
    }
  }

  return (
    <AuthLayout>
      <Card style={styles.card}>
        <Text style={styles.title}>Invitation à rejoindre Daba Syndic</Text>
        {!token && <Alert message="Ce lien d'invitation est invalide." />}
        {token && isSuccess && (
          <Text style={styles.body}>
            Invitation acceptée. Vous pouvez maintenant vous connecter pour accéder à vos lots.
          </Text>
        )}
        {token && !isSuccess && (
          <AcceptInvitationForm
            onSubmit={handleSubmit}
            isSubmitting={isPending}
            errorMessage={error ? getErrorMessage(error) : undefined}
          />
        )}
        <Button variant="secondary" onPress={() => navigation.navigate('Login')} style={styles.linkButton}>
          Retour à la connexion
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
