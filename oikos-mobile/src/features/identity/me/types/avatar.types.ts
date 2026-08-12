// Decoupled from ImagePicker's own asset type so this feature's api/hooks
// don't depend on expo-image-picker's exact shape - only the three fields
// React Native's multipart upload actually needs.
export interface PickedImageFile {
  uri: string;
  name: string;
  type: string;
}
