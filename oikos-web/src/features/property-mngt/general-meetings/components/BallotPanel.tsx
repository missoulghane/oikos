import { useState } from 'react';
import {
  useCastVote,
  useCloseVoteSession,
  useOpenVoteSession,
  useRecordShowOfHands,
} from '@/features/property-mngt/general-meetings/hooks/useMeetingMutations';
import { useAgendaItemResult } from '@/features/property-mngt/general-meetings/hooks/useAgendaItemResult';
import { useVotes } from '@/features/property-mngt/general-meetings/hooks/useVotes';
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
 * <p>Each lot carries the vote already on record for it, badge and filled
 * button. A roll call is read down a list and voted across it, and without that
 * mark the only way to know whether a lot had already been called was to
 * remember.
 *
 * <p>Collapsed unless its ballot is open. A meeting has as many panels as it
 * has agenda items, and only one of them is ever being voted on; showing them
 * all unfolded turned the session tab into a page nobody could find their
 * place in. SessionTab remounts the panel when the ballot status changes, so
 * opening a ballot unfolds it.
 *
 * <p>Closing a ballot takes away the voting controls, never the roll: the
 * per-lot detail stays readable on a closed ballot, because it is what a
 * contested result is checked against and what the minutes are written from.
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
  // A ballot that was held, open or closed. Closing it settles the votes, it does not hide
  // them: the roll is what a contested result is checked against, and what the minutes are
  // written from - it has to stay readable once the ballot is over.
  const hasBallot = item.voteSessionStatus !== 'NOT_OPENED';
  const error = openBallot.error ?? closeBallot.error ?? castVote.error ?? showOfHands.error;

  // Only while the panel is unfolded: this is one request per agenda item, and a session
  // tab holds as many panels as the meeting has points.
  const votes = useVotes(item.id, isOpenPanel && hasBallot);
  // What each lot has on record, so a vote stays readable after it is cast - the roll is
  // gone through lot by lot, and "where was I" is the question it has to answer.
  const choiceByUnitId = new Map((votes.data ?? []).map((vote) => [vote.unitId, vote.choice]));
  // Which button is actually waiting, not all of them: one mutation drives the whole roll,
  // so isPending alone would turn every button of every lot into "Chargement…".
  const pendingVote = castVote.isPending ? castVote.variables : undefined;

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
                isLoading={showOfHands.isPending && showOfHands.variables?.payload.defaultChoice === choice}
                onClick={() => showOfHands.mutate({ agendaItemId: item.id, payload: { defaultChoice: choice } })}
              >
                Tous {VOTE_CHOICE_LABELS[choice].toLowerCase()}
              </Button>
            ))}
          </div>
          <p className="text-xs text-gray-400 dark:text-gray-500">
            Le vote à main levée ne s'applique qu'aux {presentLots.length} lot(s) émargé(s) — jamais aux absents.
          </p>
        </div>
      )}

      {/* Its own block, on every ballot that was held rather than on an open one only:
          closing settles the votes, it does not put them away. */}
      {isOpenPanel && hasBallot && (
        <div className="flex flex-col gap-3">
          {/* A button, like every other control of this panel. As a bare text link under the
              show-of-hands row it read as a footnote, and the roll call - the ordinary way
              a ballot is held - looked like it did not exist. */}
          <Button
            variant="secondary"
            className="w-fit"
            aria-expanded={expanded}
            onClick={() => setExpanded((value) => !value)}
          >
            {expanded ? 'Masquer le détail des votes' : 'Détail des votes, lot par lot'}
          </Button>

          {expanded && (
            <ul className="flex flex-col gap-2">
              {presentLots.map((convocation) => {
                const castChoice = choiceByUnitId.get(convocation.unitId);
                return (
                  <li
                    key={convocation.id}
                    className="flex flex-col gap-2 rounded-lg border border-gray-200 dark:border-gray-800 p-2 sm:flex-row sm:items-center sm:justify-between"
                  >
                    <span className="flex flex-wrap items-center gap-2 text-sm text-gray-900 dark:text-white/90">
                      {formatLotLabel(convocation.unitNumber, convocation.buildingName)} ·{' '}
                      {formatWeight(convocation.votingWeight)} voix
                      {/* The badge says what is on record; the filled button says the same thing
                          where the hand is about to click. Both, because the roll is read down
                          the list and voted across it. */}
                      {castChoice ? (
                        <Badge color={VOTE_CHOICE_COLORS[castChoice]}>{VOTE_CHOICE_LABELS[castChoice]}</Badge>
                      ) : (
                        <span className="text-xs text-gray-400 dark:text-gray-500">
                          {isOpen ? 'Pas encore voté' : 'N’a pas voté'}
                        </span>
                      )}
                    </span>
                    {/* No buttons once the ballot is closed: the API refuses a vote on a closed
                        session, and offering three buttons that can only fail is worse than
                        offering none. The badge above carries the whole answer. */}
                    {isOpen && (
                      <div className="flex gap-2">
                        {CHOICES.map((choice) => (
                          <Button
                            key={choice}
                            variant={castChoice === choice ? 'primary' : 'secondary'}
                            isLoading={
                              pendingVote?.unitId === convocation.unitId && pendingVote?.choice === choice
                            }
                            onClick={() =>
                              castVote.mutate({ agendaItemId: item.id, unitId: convocation.unitId, choice })
                            }
                          >
                            {VOTE_CHOICE_LABELS[choice]}
                          </Button>
                        ))}
                      </div>
                    )}
                  </li>
                );
              })}
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
