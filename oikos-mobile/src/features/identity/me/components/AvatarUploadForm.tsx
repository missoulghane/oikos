import { useState } from 'react';
import * as ImagePicker from 'expo-image-picker';
import { Image, StyleSheet, Text, View } from 'react-native';
import type { CurrentUser } from '@/features/identity/me/types/me.types';
import { useMyAvatarUrl } from '@/features/identity/me/hooks/useMyAvatarUrl';
import { useUpdateAvatar } from '@/features/identity/me/hooks/useUpdateAvatar';
import { useRemoveAvatar } from '@/features/identity/me/hooks/useRemoveAvatar';
import { Button } from '@/shared/components/Button/Button';
import { Alert } from '@/shared/components/Alert/Alert';
import { getErrorMessage } from '@/shared/utils/getErrorMessage';
import { colors } from '@/shared/theme/colors';

// Mirrors oikos.avatar.max-file-size-bytes / allowed-content-types (oikos-api) - a
// client-side check to avoid an upload round-trip for an obviously invalid file, not
// a substitute for the server's own validation.
const MAX_FILE_SIZE_BYTES = 2 * 1024 * 1024;
const ALLOWED_CONTENT_TYPES = ['image/png', 'image/jpeg', 'image/webp'];

export function AvatarUploadForm({ user }: { user: CurrentUser }) {
  const { url } = useMyAvatarUrl(user.hasAvatar);
  const [clientError, setClientError] = useState<string | null>(null);
  const update = useUpdateAvatar();
  const remove = useRemoveAvatar();

  async function handlePickImage() {
    const permission = await ImagePicker.requestMediaLibraryPermissionsAsync();
    if (!permission.granted) {
      setClientError("Autorisez l'accès à vos photos pour changer l'avatar.");
      return;
    }
    const result = await ImagePicker.launchImageLibraryAsync({ mediaTypes: ['images'], quality: 0.8 });
    if (result.canceled) {
      return;
    }
    const asset = result.assets[0];
    const mimeType = asset.mimeType ?? 'image/jpeg';
    if (!ALLOWED_CONTENT_TYPES.includes(mimeType)) {
      setClientError('Format non pris en charge : PNG, JPEG ou WEBP uniquement.');
      return;
    }
    if (asset.fileSize && asset.fileSize > MAX_FILE_SIZE_BYTES) {
      setClientError('Le fichier dépasse la taille maximale de 2 Mo.');
      return;
    }
    setClientError(null);
    update.mutate({ uri: asset.uri, name: asset.fileName ?? 'avatar.jpg', type: mimeType });
  }

  return (
    <View style={styles.container}>
      <View style={styles.avatar}>
        {url ? <Image source={{ uri: url }} style={styles.avatarImage} /> : <Text style={styles.avatarInitial}>{user.fullName.charAt(0).toUpperCase()}</Text>}
      </View>
      <View style={styles.actions}>
        <View style={styles.buttonsRow}>
          <Button variant="secondary" isLoading={update.isPending} onPress={handlePickImage}>
            Changer la photo
          </Button>
          {user.hasAvatar && (
            <Button variant="secondary" isLoading={remove.isPending} onPress={() => remove.mutate()}>
              Supprimer
            </Button>
          )}
        </View>
        {clientError && <Alert message={clientError} />}
        {update.isError && <Alert message={getErrorMessage(update.error)} />}
        {remove.isError && <Alert message={getErrorMessage(remove.error)} />}
      </View>
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: 16,
  },
  avatar: {
    width: 80,
    height: 80,
    borderRadius: 40,
    backgroundColor: colors.gray[100],
    alignItems: 'center',
    justifyContent: 'center',
    overflow: 'hidden',
  },
  avatarImage: {
    width: '100%',
    height: '100%',
  },
  avatarInitial: {
    fontSize: 28,
    fontWeight: '600',
    color: colors.gray[400],
  },
  actions: {
    flex: 1,
    gap: 8,
  },
  buttonsRow: {
    flexDirection: 'row',
    flexWrap: 'wrap',
    gap: 8,
  },
});
