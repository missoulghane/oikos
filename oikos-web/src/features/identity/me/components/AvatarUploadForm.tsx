import { useRef, useState } from 'react';
import type { CurrentUser } from '@/features/identity/me/types/me.types';
import { useMyAvatarUrl } from '@/features/identity/me/hooks/useMyAvatarUrl';
import { useUpdateAvatar } from '@/features/identity/me/hooks/useUpdateAvatar';
import { useRemoveAvatar } from '@/features/identity/me/hooks/useRemoveAvatar';
import { Button } from '@/shared/components/Button/Button';
import { Alert } from '@/shared/components/Alert/Alert';
import { getErrorMessage } from '@/shared/utils/getErrorMessage';
import { UserCircleIcon } from '@/shared/icons';

// Mirrors oikos.avatar.max-file-size-bytes / allowed-content-types (oikos-api) - a
// client-side check to avoid an upload round-trip for an obviously invalid file, not
// a substitute for the server's own validation.
const MAX_FILE_SIZE_BYTES = 2 * 1024 * 1024;
const ALLOWED_CONTENT_TYPES = ['image/png', 'image/jpeg', 'image/webp'];

export function AvatarUploadForm({ user }: { user: CurrentUser }) {
  const { url } = useMyAvatarUrl(user.hasAvatar);
  const [clientError, setClientError] = useState<string | null>(null);
  const fileInputRef = useRef<HTMLInputElement>(null);
  const update = useUpdateAvatar();
  const remove = useRemoveAvatar();

  function handleFileChange(event: React.ChangeEvent<HTMLInputElement>) {
    const file = event.target.files?.[0];
    if (fileInputRef.current) {
      fileInputRef.current.value = '';
    }
    if (!file) {
      return;
    }
    if (!ALLOWED_CONTENT_TYPES.includes(file.type)) {
      setClientError('Format non pris en charge : PNG, JPEG ou WEBP uniquement.');
      return;
    }
    if (file.size > MAX_FILE_SIZE_BYTES) {
      setClientError('Le fichier dépasse la taille maximale de 2 Mo.');
      return;
    }
    setClientError(null);
    update.mutate(file);
  }

  return (
    <div className="flex items-center gap-4">
      <span className="flex h-20 w-20 shrink-0 items-center justify-center overflow-hidden rounded-full bg-gray-100 text-gray-400 dark:bg-white/[0.05] dark:text-gray-500">
        {url ? (
          <img src={url} alt="Avatar" className="h-full w-full object-cover" />
        ) : (
          <UserCircleIcon className="h-full w-full" />
        )}
      </span>
      <div className="flex flex-col gap-2">
        <div className="flex flex-wrap gap-2">
          <Button
            type="button"
            variant="secondary"
            isLoading={update.isPending}
            onClick={() => fileInputRef.current?.click()}
          >
            Changer la photo
          </Button>
          {user.hasAvatar && (
            <Button type="button" variant="secondary" isLoading={remove.isPending} onClick={() => remove.mutate()}>
              Supprimer
            </Button>
          )}
        </div>
        <input
          ref={fileInputRef}
          type="file"
          accept={ALLOWED_CONTENT_TYPES.join(',')}
          onChange={handleFileChange}
          className="hidden"
        />
        {clientError && <Alert message={clientError} />}
        {update.isError && <Alert message={getErrorMessage(update.error)} />}
        {remove.isError && <Alert message={getErrorMessage(remove.error)} />}
      </div>
    </div>
  );
}
