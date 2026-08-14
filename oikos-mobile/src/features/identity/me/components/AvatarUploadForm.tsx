import { useState } from 'react';
import * as ImagePicker from 'expo-image-picker';
import { ImageManipulator, SaveFormat } from 'expo-image-manipulator';
import { Asset } from 'expo-asset';
import { Image, Modal, Pressable, StyleSheet, Text, View } from 'react-native';
import type { CurrentUser } from '@/features/identity/me/types/me.types';
import { useMyAvatarUrl } from '@/features/identity/me/hooks/useMyAvatarUrl';
import { useUpdateAvatar } from '@/features/identity/me/hooks/useUpdateAvatar';
import { useRemoveAvatar } from '@/features/identity/me/hooks/useRemoveAvatar';
import { AVATAR_PRESETS, type AvatarPreset } from '@/features/identity/me/constants/avatarPresets';
import { Button } from '@/shared/components/Button/Button';
import { Alert } from '@/shared/components/Alert/Alert';
import { getErrorMessage } from '@/shared/utils/getErrorMessage';
import { colors } from '@/shared/theme/colors';

// Mirrors oikos.avatar.max-file-size-bytes / allowed-content-types (oikos-api) - a
// client-side check to avoid an upload round-trip for an obviously invalid file, not
// a substitute for the server's own validation. Photos are also downscaled below
// before this check runs, so a phone photo only hits this limit if it's genuinely
// an unusual file, not because a selfie is a few MB.
const MAX_FILE_SIZE_BYTES = 5 * 1024 * 1024;
const MAX_DIMENSION_PX = 512;
const ALLOWED_CONTENT_TYPES = ['image/png', 'image/jpeg', 'image/webp'];

export function AvatarUploadForm({ user }: { user: CurrentUser }) {
  const { url } = useMyAvatarUrl(user.hasAvatar);
  const [clientError, setClientError] = useState<string | null>(null);
  const [isPickerOpen, setIsPickerOpen] = useState(false);
  const update = useUpdateAvatar();
  const remove = useRemoveAvatar();

  async function handlePickImage() {
    // No permission request needed for an images-only picker: the PHPicker
    // iOS 14+/Android photo picker used here run out-of-process and never
    // need library access granted - requesting it anyway was triggering the
    // legacy full-library prompt, which could hang the picker on Simulator.
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

    // height omitted so the aspect ratio is preserved - the circular avatar
    // frame crops it visually, but the uploaded file itself must stay undistorted.
    const context = ImageManipulator.manipulate(asset.uri);
    context.resize({ width: MAX_DIMENSION_PX });
    const rendered = await context.renderAsync();
    const resized = await rendered.saveAsync({ format: SaveFormat.JPEG, compress: 0.85 });

    const info = await fetch(resized.uri);
    const blob = await info.blob();
    if (blob.size > MAX_FILE_SIZE_BYTES) {
      setClientError('Le fichier dépasse la taille maximale de 5 Mo.');
      return;
    }

    setClientError(null);
    setIsPickerOpen(false);
    update.mutate({ uri: resized.uri, name: asset.fileName ?? 'avatar.jpg', type: 'image/jpeg' });
  }

  async function handleSelectPreset(preset: AvatarPreset) {
    const asset = Asset.fromModule(preset.source);
    await asset.downloadAsync();
    const uri = asset.localUri ?? asset.uri;
    setClientError(null);
    setIsPickerOpen(false);
    update.mutate({ uri, name: `${preset.id}.png`, type: 'image/png' });
  }

  return (
    <View style={styles.container}>
      <View style={styles.row}>
        <View style={styles.avatar}>
          {url ? <Image source={{ uri: url }} style={styles.avatarImage} /> : <Text style={styles.avatarInitial}>{user.fullName.charAt(0).toUpperCase()}</Text>}
        </View>
        <View style={styles.actions}>
          <View style={styles.buttonsRow}>
            <Button variant="secondary" isLoading={update.isPending} onPress={() => setIsPickerOpen(true)}>
              Changer l&apos;avatar
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

      <Modal
        visible={isPickerOpen}
        transparent
        animationType="fade"
        onRequestClose={() => setIsPickerOpen(false)}
      >
        <Pressable style={styles.backdrop} onPress={() => setIsPickerOpen(false)}>
          {/* Stops a tap inside the sheet from reaching the backdrop's dismiss. */}
          <Pressable style={styles.sheet} onPress={() => {}}>
            <Text style={styles.sheetTitle}>Changer l&apos;avatar</Text>
            <Text style={styles.sheetSubtitle}>Choisir un avatar de la bibliothèque</Text>
            <View style={styles.presetGrid}>
              {AVATAR_PRESETS.map((preset) => (
                <Pressable
                  key={preset.id}
                  accessibilityLabel={preset.label}
                  onPress={() => handleSelectPreset(preset)}
                  disabled={update.isPending}
                  style={styles.presetItem}
                >
                  <Image source={preset.source} style={styles.presetImage} />
                </Pressable>
              ))}
            </View>
            <View style={styles.sheetFooter}>
              <Button variant="secondary" isLoading={update.isPending} onPress={handlePickImage}>
                Importer une photo
              </Button>
              <Button variant="secondary" onPress={() => setIsPickerOpen(false)}>
                Annuler
              </Button>
            </View>
          </Pressable>
        </Pressable>
      </Modal>
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    gap: 16,
  },
  row: {
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
  backdrop: {
    flex: 1,
    backgroundColor: 'rgba(16, 24, 40, 0.5)',
    alignItems: 'center',
    justifyContent: 'center',
    padding: 24,
  },
  sheet: {
    width: '100%',
    maxWidth: 340,
    gap: 8,
    borderRadius: 20,
    backgroundColor: colors.white,
    padding: 20,
  },
  sheetTitle: {
    fontSize: 16,
    fontWeight: '600',
    color: colors.gray[900],
  },
  sheetSubtitle: {
    fontSize: 13,
    color: colors.gray[500],
  },
  sheetFooter: {
    gap: 8,
    borderTopWidth: 1,
    borderTopColor: colors.gray[200],
    paddingTop: 16,
  },
  presetGrid: {
    flexDirection: 'row',
    flexWrap: 'wrap',
    justifyContent: 'space-between',
    gap: 12,
    paddingVertical: 8,
  },
  presetItem: {
    width: 60,
    height: 60,
    borderRadius: 30,
    overflow: 'hidden',
    borderWidth: 1,
    borderColor: colors.gray[200],
  },
  presetImage: {
    width: '100%',
    height: '100%',
  },
});
