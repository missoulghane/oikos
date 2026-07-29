import { useMutation } from '@tanstack/react-query';
import { inviteParty } from '@/features/property-mngt/parties/api/inviteParty';

export function useInviteParty() {
  return useMutation({
    mutationFn: inviteParty,
  });
}
