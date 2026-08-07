import { useEffect, useRef, useState } from 'react';
import { Link, useSearchParams } from 'react-router-dom';
import { AuthLayout } from '@/shared/layouts/AuthLayout';
import { Card } from '@/shared/components/Card/Card';
import { Loader } from '@/shared/components/Loader/Loader';
import { Alert } from '@/shared/components/Alert/Alert';
import { verifyAccount } from '@/features/identity/register/api/verifyAccount';
import { getErrorMessage } from '@/shared/utils/getErrorMessage';
import { sanitizeReturnTo } from '@/shared/utils/sanitizeReturnTo';

type VerificationStatus = 'pending' | 'success' | 'error';

export function VerifyEmailPage() {
  const [searchParams] = useSearchParams();
  const token = searchParams.get('token');
  const returnTo = sanitizeReturnTo(searchParams.get('returnTo'));
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
      <Card>
        <h2 className="mb-4 text-lg font-semibold text-gray-900">Vérification de l'email</h2>
        {!token && <Alert message="Ce lien de vérification est invalide." />}
        {token && status === 'pending' && <Loader label="Vérification en cours…" />}
        {token && status === 'success' && (
          <p className="text-sm text-gray-600">Votre email a bien été vérifié. Vous pouvez maintenant vous connecter.</p>
        )}
        {token && status === 'error' && <Alert message={errorMessage ?? 'La vérification a échoué.'} />}
        <Link
          to={returnTo ? `/login?returnTo=${encodeURIComponent(returnTo)}` : '/login'}
          className="mt-4 inline-block text-sm font-medium text-gray-900 underline"
        >
          Retour à la connexion
        </Link>
      </Card>
    </AuthLayout>
  );
}
