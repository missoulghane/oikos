import { useCurrentUser } from '@/features/identity/me/hooks/useCurrentUser';
import { EditProfileForm } from '@/features/identity/me/components/EditProfileForm';
import { AvatarUploadForm } from '@/features/identity/me/components/AvatarUploadForm';
import { ChangePasswordForm } from '@/features/identity/me/components/ChangePasswordForm';
import { Card } from '@/shared/components/Card/Card';
import { Loader } from '@/shared/components/Loader/Loader';
import { Alert } from '@/shared/components/Alert/Alert';
import { getErrorMessage } from '@/shared/utils/getErrorMessage';

export function ProfilePage() {
  const currentUser = useCurrentUser();

  return (
    <div className="flex flex-col gap-6">
      <h1 className="text-lg font-semibold text-gray-900 dark:text-white/90">Mon profil</h1>

      {currentUser.isLoading && <Loader label="Chargement de votre profil…" />}
      {currentUser.isError && <Alert message={getErrorMessage(currentUser.error)} />}

      {currentUser.data && (
        <>
          <Card className="flex w-full flex-col gap-2">
            <AvatarUploadForm user={currentUser.data} />
          </Card>

          <Card className="flex w-full flex-col gap-2">
            <EditProfileForm user={currentUser.data} />
          </Card>

          <Card className="flex w-full flex-col gap-2">
            <h2 className="text-base font-semibold text-gray-900 dark:text-white/90">Mot de passe</h2>
            <ChangePasswordForm />
          </Card>
        </>
      )}
    </div>
  );
}
