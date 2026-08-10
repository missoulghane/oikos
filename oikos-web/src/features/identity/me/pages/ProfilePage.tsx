import { useCurrentUser } from '@/features/identity/me/hooks/useCurrentUser';
import { EditProfileForm } from '@/features/identity/me/components/EditProfileForm';
import { Card } from '@/shared/components/Card/Card';
import { Loader } from '@/shared/components/Loader/Loader';
import { Alert } from '@/shared/components/Alert/Alert';
import { getErrorMessage } from '@/shared/utils/getErrorMessage';

export function ProfilePage() {
  const currentUser = useCurrentUser();

  return (
    <div className="flex flex-col gap-6">
      <h1 className="text-lg font-semibold text-gray-900 dark:text-white/90">Mes informations</h1>

      <Card className="flex max-w-md flex-col gap-2">
        {currentUser.isLoading && <Loader label="Chargement de votre profil…" />}
        {currentUser.isError && <Alert message={getErrorMessage(currentUser.error)} />}
        {currentUser.data && <EditProfileForm user={currentUser.data} />}
      </Card>
    </div>
  );
}
