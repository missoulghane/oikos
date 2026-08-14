import { useRef, useState } from 'react';
import type { CurrentUser } from '@/features/identity/me/types/me.types';
import { useMyAvatarUrl } from '@/features/identity/me/hooks/useMyAvatarUrl';
import { useUpdateAvatar } from '@/features/identity/me/hooks/useUpdateAvatar';
import { useRemoveAvatar } from '@/features/identity/me/hooks/useRemoveAvatar';
import { AVATAR_PRESETS } from '@/features/identity/me/constants/avatarPresets';
import { Button } from '@/shared/components/Button/Button';
import { Modal } from '@/shared/components/Modal/Modal';
import { Alert } from '@/shared/components/Alert/Alert';
import { getErrorMessage } from '@/shared/utils/getErrorMessage';
import { resizeImageFile } from '@/shared/utils/resizeImageFile';
import { UserCircleIcon } from '@/shared/icons';

// Mirrors oikos.avatar.max-file-size-bytes / allowed-content-types (oikos-api) - a
// client-side check to avoid an upload round-trip for an obviously invalid file, not
// a substitute for the server's own validation. Photos are also downscaled below
// (see resizeImageFile) before this check runs, so a phone photo only hits this
// limit if it's genuinely an unusual file, not because a selfie is a few MB.
const MAX_FILE_SIZE_BYTES = 5 * 1024 * 1024;
const MAX_DIMENSION_PX = 512;
const ALLOWED_CONTENT_TYPES = ['image/png', 'image/jpeg', 'image/webp'];

export function AvatarUploadForm({ user }: { user: CurrentUser }) {
  const { url } = useMyAvatarUrl(user.hasAvatar);
  const [clientError, setClientError] = useState<string | null>(null);
  const [isPickerOpen, setIsPickerOpen] = useState(false);
  const fileInputRef = useRef<HTMLInputElement>(null);
  const update = useUpdateAvatar();
  const remove = useRemoveAvatar();

  async function handleFileChange(event: React.ChangeEvent<HTMLInputElement>) {
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
    const resized = await resizeImageFile(file, MAX_DIMENSION_PX);
    if (resized.size > MAX_FILE_SIZE_BYTES) {
      setClientError('Le fichier dépasse la taille maximale de 5 Mo.');
      return;
    }
    setClientError(null);
    setIsPickerOpen(false);
    update.mutate(resized);
  }

  async function handleSelectPreset(preset: (typeof AVATAR_PRESETS)[number]) {
    const response = await fetch(preset.url);
    const blob = await response.blob();
    const file = new File([blob], `${preset.id}.png`, { type: 'image/png' });
    setClientError(null);
    setIsPickerOpen(false);
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
          <Button type="button" variant="secondary" isLoading={update.isPending} onClick={() => setIsPickerOpen(true)}>
            Changer l&rsquo;avatar
          </Button>
          {user.hasAvatar && (
            <Button type="button" variant="secondary" isLoading={remove.isPending} onClick={() => remove.mutate()}>
              Supprimer
            </Button>
          )}
        </div>
        {clientError && <Alert message={clientError} />}
        {update.isError && <Alert message={getErrorMessage(update.error)} />}
        {remove.isError && <Alert message={getErrorMessage(remove.error)} />}
      </div>

      <Modal isOpen={isPickerOpen} onClose={() => setIsPickerOpen(false)} title="Changer l'avatar">
        <div className="flex flex-col gap-4">
          <div>
            <p className="text-sm text-gray-500 dark:text-gray-400">Choisir un avatar de la bibliothèque</p>
            <div className="mt-3 grid grid-cols-4 gap-3">
              {AVATAR_PRESETS.map((preset) => (
                <button
                  key={preset.id}
                  type="button"
                  title={preset.label}
                  onClick={() => handleSelectPreset(preset)}
                  disabled={update.isPending}
                  className="aspect-square overflow-hidden rounded-full ring-1 ring-inset ring-gray-200 transition hover:ring-2 hover:ring-brand-500 disabled:opacity-50 dark:ring-gray-700"
                >
                  <img src={preset.url} alt={preset.label} className="h-full w-full" />
                </button>
              ))}
            </div>
          </div>
          <div className="border-t border-gray-200 pt-4 dark:border-gray-800">
            <Button
              type="button"
              variant="secondary"
              isLoading={update.isPending}
              onClick={() => fileInputRef.current?.click()}
            >
              Importer une photo
            </Button>
          </div>
        </div>
      </Modal>

      <input
        ref={fileInputRef}
        type="file"
        accept={ALLOWED_CONTENT_TYPES.join(',')}
        onChange={handleFileChange}
        className="hidden"
      />
    </div>
  );
}
