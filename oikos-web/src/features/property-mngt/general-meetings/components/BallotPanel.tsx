import { useState } from 'react';
import {
  useCastVote,
  useCloseVoteSession,
  useOpenVoteSession,
  useRecordShowOfHands,
} from '@/features/property-mngt/general-meetings/hooks/useMeetingMutations';
import { useAgendaItemResult } from '@/features/property-mngt/general-meetings/hooks/useAgendaItemResult';
import {
  MAJORITY_RULE_LABELS,
  VOTE_CHOICE_COLORS,
  VOTE_CHOICE_LABELS,
  VOTE_OUTCOME_LABELS,
  VOTE_SESSION_STATUS_COLORS,
  VOTE_SESSION_STATUS_LABELS,
} from '@/features/property-mngt/general-meetings/constants/generalMeetingLabels';
import { formatLotLabel, formatWeight } from '@/features/property-mngt/general-meetings/utils/formatMeeting';
import type { AgendaItem } from '@/features/property-mngt/general-meetings/types/generalMeeting.types';
import type { Convocation } from '@/features/property-mngt/general-meetings/types/convocation.types';
import type { VoteChoice } from '@/features/property-mngt/general-meetings/types/vote.types';
import { Alert } from '@/shared/components/Alert/Alert';
import { Badge } from '@/shared/components/Badge/Badge';
import { Button } from '@/shared/components/Button/Button';
import { Card } from '@/shared/components/Card/Card';
import { getErrorMessage } from '@/shared/utils/getErrorMessage';

const CHOICES: VoteChoice[] = ['FOR', 'AGAINST', 'ABSTENTION'];

interface BallotPanelProps {
  meetingId: string;
  item: AgendaItem;
  position: number;
  convocations: Convocation[];
  sessionInProgress: boolean;
}

/**
 * One ballot: open it, record the votes (by hand or as a show of hands), close
 * it, read the result.
 *
 * <p>Only the lots signed in appear as voters, because only they may vote -
 * and the show of hands applies to exactly that set. The three denominators
 * are all displayed under the result: the applied rule uses one of them, and a
 * contested outcome has to be readable against the other two.
 *
 * <p>Collapsed unless its ballot is open. A meeting has as many panels as it
 * has agenda items, and only one of them is ever being voted on; showing them
 * all unfolded turned the session tab into a page nobody could find their
 * place in. SessionTab remounts the panel when the ballot status changes, so
 * opening a ballot unfolds it.
 */
export function BallotPanel({ meetingId, item, position, convocations, sessionInProgress }: BallotPanelProps) {
  const openBallot = useOpenVoteSession(meetingId);
  const closeBallot = useCloseVoteSession(meetingId);
  const castVote = useCastVote();
  const showOfHands = useRecordShowOfHands();
  const result = useAgendaItemResult(item.id, item.voteSessionStatus !== 'NOT_OPENED');
  const [isOpenPanel, setIsOpenPanel] = useState(item.voteSessionStatus === 'OPEN');
  const [expanded, setExpanded] = useState(false);

  const presentLots = convocations.filter((convocation) => convocation.checkedIn);
  const isOpen = item.voteSessionStatus === 'OPEN';
  const error = openBallot.error ?? closeBallot.error ?? castVote.error ?? showOfHands.error;

  return (
    <Card className="flex flex-col gap-4">
      <div className="flex flex-wrap items-start justify-between gap-2">
        <div className="flex flex-col gap-1">
          <div className="flex flex-wrap items-center gap-2">
            <span className="text-sm text-gray-400 dark:text-gray-500">#{position}</span>
            <span className="font-medium text-gray-900 dark:text-white/90">{item.label}</span>
            <Badge color={VOTE_SESSION_STATUS_COLORS[item.voteSessionStatus]}>
              {VOTE_SESSION_STATUS_LABELS[item.voteSessionStatus]}
            </Badge>
          </div>
          <span className="text-sm text-gray-500 dark:text-gray-400">
            {MAJORITY_RULE_LABELS[item.majorityRule]}
          </span>
        </div>
        <div className="flex flex-wrap gap-2">
          <Button
            variant="secondary"
            aria-expanded={isOpenPanel}
            onClick={() => setIsOpenPanel((value) => !value)}
          >
            {isOpenPanel ? 'Replier' : 'Déplier'}
          </Button>
          {item.voteSessionStatus === 'NOT_OPENED' && (
            <Button
              variant="secondary"
              disabled={!sessionInProgress}
              isLoading={openBallot.isPending}
              onClick={() => openBallot.mutate(item.id)}
            >
              Ouvrir le scrutin
            </Button>
          )}
          {isOpen && (
            <Button variant="secondary" isLoading={closeBallot.isPending} onClick={() => closeBallot.mutate(item.id)}>
              Clore le scrutin
            </Button>
          )}
        </div>
      </div>

      {error && <Alert message={getErrorMessage(error)} />}

      {isOpenPanel && isOpen && (
        <div className="flex flex-col gap-3">
          <div className="flex flex-wrap items-center gap-2">
            <span className="text-sm text-gray-500 dark:text-gray-400">À main levée :</span>
            {CHOICES.map((choice) => (
              <Button
                key={choice}
                variant="secondary"
                isLoading={showOfHands.isPending}
                onClick={() => showOfHands.mutate({ agendaItemId: item.id, payload: { defaultChoice: choice } })}
              >
                Tous {VOTE_CHOICE_LABELS[choice].toLowerCase()}
              </Button>
            ))}
          </div>
          <p className="text-xs text-gray-400 dark:text-gray-500">
            Le vote à main levée ne s'applique qu'aux {presentLots.length} lot(s) émargé(s) — jamais aux absents.
          </p>

          <button
            type="button"
            onClick={() => setExpanded((value) => !value)}
            className="w-fit text-sm text-brand-500 hover:underline"
          >
            {expanded ? 'Masquer le vote nominatif' : 'Vote nominatif, lot par lot'}
          </button>

          {expanded && (
            <ul className="flex flex-col gap-2">
              {presentLots.map((convocation) => (
                <li
                  key={convocation.id}
                  className="flex flex-col gap-2 rounded-lg border border-gray-200 dark:border-gray-800 p-2 sm:flex-row sm:items-center sm:justify-between"
                >
                  <span className="text-sm text-gray-900 dark:text-white/90">
                    {formatLotLabel(convocation.unitNumber, convocation.buildingName)} ·{' '}
                    {formatWeight(convocation.votingWeight)} voix
                  </span>
                  <div className="flex gap-2">
                    {CHOICES.map((choice) => (
                      <Button
                        key={choice}
                        variant="secondary"
                        isLoading={castVote.isPending}
                        onClick={() =>
                          castVote.mutate({ agendaItemId: item.id, unitId: convocation.unitId, choice })
                        }
                      >
                        {VOTE_CHOICE_LABELS[choice]}
                      </Button>
                    ))}
                  </div>
                </li>
              ))}
            </ul>
          )}
        </div>
      )}

      {/* The result stays visible while folded: it is the one line a syndic scans down the
          list for. Everything else - the voting controls - is what folds away. */}
      {result.data && (
        <div className="flex flex-col gap-2 rounded-xl bg-gray-50 dark:bg-white/[0.03] p-3">
          <div className="flex flex-wrap items-center gap-3">
            {CHOICES.map((choice) => {
              const counts = {
                FOR: [result.data.forCount, result.data.forWeight],
                AGAINST: [result.data.againstCount, result.data.againstWeight],
                ABSTENTION: [result.data.abstentionCount, result.data.abstentionWeight],
              }[choice];
              return (
                <span key={choice} className="flex items-center gap-1 text-sm">
                  <Badge color={VOTE_CHOICE_COLORS[choice]}>{VOTE_CHOICE_LABELS[choice]}</Badge>
                  <span className="text-gray-600 dark:text-gray-300">
                    {counts[0]} lot(s) · {formatWeight(counts[1])} voix
                  </span>
                </span>
              );
            })}
          </div>
          {isOpenPanel && (
            <p className="text-xs text-gray-500 dark:text-gray-400">
              Voix exprimées {formatWeight(result.data.expressedWeight)} · présentes{' '}
              {formatWeight(result.data.presentWeight)} · copropriété {formatWeight(result.data.totalWeight)}
            </p>
          )}
          <p className="text-sm font-medium text-gray-900 dark:text-white/90">
            {item.voteSessionStatus === 'CLOSED' ? 'Résolution' : 'Tendance'} :{' '}
            {VOTE_OUTCOME_LABELS[result.data.outcome].toLowerCase()}
          </p>
        </div>
      )}
    </Card>
  );
}
