import { useMutation } from '@tanstack/react-query';
import { inviteParty } from '@/features/property-mngt/parties/api/inviteParty';

export interface BulkInviteResult {
  invited: number;
  alreadyLinked: number;
  failed: number;
}

export function useBulkInviteParties() {
  return useMutation({
    mutationFn: async (partyIds: string[]): Promise<BulkInviteResult> => {
      const results = await Promise.allSettled(partyIds.map((partyId) => inviteParty(partyId)));
      const summary: BulkInviteResult = { invited: 0, alreadyLinked: 0, failed: 0 };
      for (const result of results) {
        if (result.status === 'fulfilled') {
          if (result.value.invited) {
            summary.invited += 1;
          } else {
            summary.alreadyLinked += 1;
          }
        } else {
          summary.failed += 1;
        }
      }
      return summary;
    },
  });
}
