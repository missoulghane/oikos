import { useEffect, useState } from 'react';
import { useQuery } from '@tanstack/react-query';
import { getMyAvatar } from '@/features/identity/me/api/getMyAvatar';
import { queryKeys } from '@/shared/constants/queryKeys';

/**
 * GET /users/me/avatar requires the Bearer auth header, so an <Image
 * source={{uri}}> can't load it directly. Unlike oikos-web (which exposes the
 * fetched Blob as a `URL.createObjectURL` blob: URL - not reliably supported
 * for <Image> on React Native), this reads the Blob through FileReader into a
 * base64 data: URI instead, which <Image> can render directly on both platforms.
 */
export function useMyAvatarUrl(hasAvatar: boolean) {
  const avatar = useQuery({
    queryKey: queryKeys.me.avatar(hasAvatar),
    queryFn: getMyAvatar,
    enabled: hasAvatar,
  });
  const [url, setUrl] = useState<string | null>(null);

  useEffect(() => {
    if (!avatar.data) {
      setUrl(null);
      return;
    }
    const reader = new FileReader();
    reader.onload = () => setUrl(typeof reader.result === 'string' ? reader.result : null);
    reader.readAsDataURL(avatar.data);
  }, [avatar.data]);

  return { url, isLoading: avatar.isLoading };
}
