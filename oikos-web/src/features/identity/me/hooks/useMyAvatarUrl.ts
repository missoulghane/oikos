import { useEffect, useMemo } from 'react';
import { useQuery } from '@tanstack/react-query';
import { getMyAvatar } from '@/features/identity/me/api/getMyAvatar';
import { queryKeys } from '@/shared/constants/queryKeys';

/**
 * GET /users/me/avatar requires the Bearer auth header, so a plain <img src="…">
 * can't load it directly - fetched as a Blob through httpClient instead, then
 * exposed as an object URL. Revoked on every change/unmount so switching or
 * removing the avatar doesn't leak the previous blob URL.
 */
export function useMyAvatarUrl(hasAvatar: boolean) {
  const avatar = useQuery({
    queryKey: queryKeys.me.avatar(hasAvatar),
    queryFn: getMyAvatar,
    enabled: hasAvatar,
  });
  const url = useMemo(() => (avatar.data ? URL.createObjectURL(avatar.data) : null), [avatar.data]);

  useEffect(() => {
    return () => {
      if (url) {
        URL.revokeObjectURL(url);
      }
    };
  }, [url]);

  return { url, isLoading: avatar.isLoading };
}
