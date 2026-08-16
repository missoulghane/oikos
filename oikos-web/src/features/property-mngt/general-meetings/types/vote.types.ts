import type { MajorityRule, VoteSessionStatus } from '@/features/property-mngt/general-meetings/types/generalMeeting.types';

export type VoteChoice = 'FOR' | 'AGAINST' | 'ABSTENTION';

export type VoteOutcome = 'ADOPTED' | 'REJECTED';

export interface Vote {
  id: string;
  agendaItemId: string;
  unitId: string;
  unitNumber: string | null;
  buildingName: string | null;
  votingWeight: number;
  choice: VoteChoice;
  castAt: string;
}

/**
 * The three denominators are all returned, not just the one the applied rule
 * used: a contested result has to be readable against the other readings too.
 */
export interface AgendaItemResult {
  agendaItemId: string;
  label: string;
  majorityRule: MajorityRule;
  voteSessionStatus: VoteSessionStatus;
  forCount: number;
  againstCount: number;
  abstentionCount: number;
  forWeight: number;
  againstWeight: number;
  abstentionWeight: number;
  expressedWeight: number;
  presentWeight: number;
  totalWeight: number;
  outcome: VoteOutcome;
}

export interface ShowOfHandsPayload {
  defaultChoice: VoteChoice;
  exceptions?: Record<string, VoteChoice>;
}
