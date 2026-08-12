import { useEffect, useRef, useState } from 'react';
import { StyleSheet, Text } from 'react-native';
import type { NativeStackScreenProps } from '@react-navigation/native-stack';
import { AuthLayout } from '@/shared/layouts/AuthLayout';
import { Card } from '@/shared/components/Card/Card';
import { Loader } from '@/shared/components/Loader/Loader';
import { Alert } from '@/shared/components/Alert/Alert';
import { Button } from '@/shared/components/Button/Button';
import { verifyAccount } from '@/features/identity/register/api/verifyAccount';
import { getErrorMessage } from '@/shared/utils/getErrorMessage';
import { colors } from '@/shared/theme/colors';
import type { AuthStackParamList } from '@/app/navigation/AuthNavigator';

type Props = NativeStackScreenProps<AuthStackParamList, 'VerifyEmail'>;
type VerificationStatus = 'pending' | 'success' | 'error';

export function VerifyEmailScreen({ route, navigation }: Props) {
  const { token } = route.params ?? {};
  const [status, setStatus] = useState<VerificationStatus>('pending');
  const [errorMessage, setErrorMessage] = useState<string>();
  const hasRequested = useRef(false);

  useEffect(() => {
    if (!token || hasRequested.current) {
      return;
    }
    hasRequested.current = true;
    verifyAccount({ token })
      .then(() => setStatus('success'))
      .catch((error: unknown) => {
        setErrorMessage(getErrorMessage(error));
        setStatus('error');
      });
  }, [token]);

  return (
    <AuthLayout>
      <Card style={styles.card}>
        <Text style={styles.title}>Vérification de l'email</Text>
        {!token && <Alert message="Ce lien de vérification est invalide." />}
        {token && status === 'pending' && <Loader label="Vérification en cours…" />}
        {token && status === 'success' && (
          <Text style={styles.body}>Votre email a bien été vérifié. Vous pouvez maintenant vous connecter.</Text>
        )}
        {token && status === 'error' && <Alert message={errorMessage ?? 'La vérification a échoué.'} />}
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
