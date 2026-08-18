import { useMemo, useState } from 'react';
import { useOutletContext } from 'react-router-dom';
import type { GeneralMeetingContext } from '@/features/property-mngt/general-meetings/pages/GeneralMeetingDetailLayout';
import { useConvocationChannels } from '@/features/property-mngt/general-meetings/hooks/useConvocationChannels';
import { useConvocations } from '@/features/property-mngt/general-meetings/hooks/useConvocations';
import { useAttendanceSummary } from '@/features/property-mngt/general-meetings/hooks/useAttendanceSummary';
import {
  useGenerateConvocations,
  useRemindConvocations,
  useScheduleGeneralMeeting,
  useSendPendingConvocations,
} from '@/features/property-mngt/general-meetings/hooks/useMeetingMutations';
import { AttendanceSummaryCard } from '@/features/property-mngt/general-meetings/components/AttendanceSummaryCard';
import { ConvocationFilters } from '@/features/property-mngt/general-meetings/components/ConvocationFilters';
import { ConvocationTrackingTable } from '@/features/property-mngt/general-meetings/components/ConvocationTrackingTable';
import {
  CONVOCATION_SORT_KEYS,
  DEFAULT_CONVOCATION_FILTERS,
  filterAndSortConvocations,
  pendingCountOn,
  type ConvocationFiltersValue,
  type ConvocationSortField,
} from '@/features/property-mngt/general-meetings/utils/filterConvocations';
import { Alert } from '@/shared/components/Alert/Alert';
import { Button } from '@/shared/components/Button/Button';
import { Card } from '@/shared/components/Card/Card';
import { EmptyState } from '@/shared/components/EmptyState/EmptyState';
import { FilterPanel } from '@/shared/components/FilterPanel/FilterPanel';
import { Loader } from '@/shared/components/Loader/Loader';
import { Pagination } from '@/shared/components/Pagination/Pagination';
import { countActiveFilters } from '@/shared/utils/countActiveFilters';
import { getErrorMessage } from '@/shared/utils/getErrorMessage';
import { nextSortDirection } from '@/shared/utils/sorting';

/**
 * Two acts, in the order they are performed: generate, then send. They used to
 * be one button, whose failure mode - "some emails went out, some didn't, and
 * the meeting is convened either way" - could not be explained to anyone.
 *
 * <p>Only one of them is on screen at a time. Before anything is generated
 * there is nothing to send, and a sending panel saying "générez d'abord" is a
 * step the syndic cannot take dressed up as one he can. Afterwards, generating
 * is no longer the task at hand and steps back to a single line.
 *
 * <p>Sending is one button per channel rather than a dropdown and one button:
 * the dropdown made the channel a setting, when it is the action itself. What
 * the buttons are comes from the server's catalog, so a channel added there
 * appears here without a release - which is the whole reason the channels are
 * data.
 */

/** Deliberately not 5: this table is scanned for the lots still silent, and
 *  sixty lots at five a page is twelve pages of scanning. */
const PAGE_SIZE = 20;

export function ConvocationsTab() {
  const { property, meeting } = useOutletContext<GeneralMeetingContext>();
  const convocations = useConvocations(meeting.id);
  const channels = useConvocationChannels();
  const summary = useAttendanceSummary(meeting.id);
  const generate = useGenerateConvocations(meeting.id);
  const sendPending = useSendPendingConvocations(meeting.id);
  const schedule = useScheduleGeneralMeeting(meeting.id);
  const remind = useRemindConvocations(meeting.id);

  const [filters, setFilters] = useState<ConvocationFiltersValue>(DEFAULT_CONVOCATION_FILTERS);
  const [page, setPage] = useState(0);

  const rows = useMemo(() => convocations.data ?? [], [convocations.data]);
  const channelList = channels.data ?? [];
  // Only the channels the application can perform itself get a button. The postal ones are
  // recorded from a convocation's own page once a person has actually posted something -
  // pressing a button must never be able to claim a letter left the building.
  const automatedChannels = channelList.filter((channel) => channel.automated);

  const hasAgenda = meeting.agendaItemCount > 0;
  const hasDateAndVenue = Boolean(meeting.scheduledAt && meeting.venueType);
  // Which of the two panels to show is only knowable once the convocations are in. Deciding
  // on an empty array while the query is still in flight would flash "Générer" at a syndic
  // whose convocations left last week, then swap it for "Envoyer" under his cursor.
  const isLoaded = convocations.data !== undefined;
  const isGenerated = rows.length > 0;
  const canGenerate =
    hasAgenda && hasDateAndVenue && meeting.status !== 'CLOSED' && meeting.status !== 'MINUTES_PUBLISHED';

  const visible = useMemo(() => filterAndSortConvocations(rows, filters), [rows, filters]);
  const totalPages = Math.max(1, Math.ceil(visible.length / PAGE_SIZE));
  // Clamped rather than reset by an effect: a filter narrowing the list while the syndic sits
  // on page 4 must not leave him staring at an empty table.
  const currentPage = Math.min(page, totalPages - 1);
  const pageRows = visible.slice(currentPage * PAGE_SIZE, currentPage * PAGE_SIZE + PAGE_SIZE);

  function handleFiltersChange(next: ConvocationFiltersValue) {
    setFilters(next);
    setPage(0);
  }

  function handleSort(field: ConvocationSortField) {
    handleFiltersChange({
      ...filters,
      sortBy: field,
      sortDirection: nextSortDirection(field, filters.sortBy, filters.sortDirection),
    });
  }

  /**
   * Generating convokes the meeting when it is still a draft: the date and the
   * venue were settled at creation, so there is nothing left to ask, and the
   * DRAFT -> SCHEDULED transition is what "ready to convoke" means. It sends
   * nothing - that is the panel below.
   */
  async function generateAll() {
    if (meeting.status === 'DRAFT' && meeting.scheduledAt && meeting.venueType) {
      await schedule.mutateAsync({
        scheduledAt: meeting.scheduledAt,
        venueType: meeting.venueType,
        venueAddress: meeting.venueAddress,
        venueLink: meeting.venueLink,
      });
    }
    await generate.mutateAsync();
  }

  const isGenerating = generate.isPending || schedule.isPending;

  return (
    <div className="flex flex-col gap-6">
      {!hasAgenda && (
        <Alert
          variant="warning"
          message="Ajoutez au moins un point à l'ordre du jour avant de convoquer les copropriétaires."
        />
      )}
      {!hasDateAndVenue && (
        <Alert variant="warning" message="Cette assemblée n'a ni date ni lieu : elle ne peut pas être convoquée." />
      )}

      {/* Without the émargement count and the quorum badge: this tab handles the convocation,
          from its sending to the opening of the session, and nobody has signed in yet. Both
          live on the Séance tab, where they change as the room fills. */}
      {summary.data && <AttendanceSummaryCard summary={summary.data} showAttendance={false} />}

      {isLoaded && !isGenerated && (
        <Card className="flex flex-col gap-4">
          <div>
            <h3 className="font-medium text-gray-900 dark:text-white/90">Générer les convocations</h3>
            <p className="text-sm text-gray-500 dark:text-gray-400">
              Une convocation est créée pour <strong>chaque lot</strong> de la copropriété, y compris ceux sans
              copropriétaire rattaché : ils comptent dans le total des voix. Rien n'est envoyé à cette étape.
            </p>
          </div>
          <div>
            <Button isLoading={isGenerating} disabled={!canGenerate} onClick={generateAll}>
              Générer les convocations
            </Button>
          </div>
          {schedule.isError && <Alert message={getErrorMessage(schedule.error)} />}
          {generate.isError && <Alert message={getErrorMessage(generate.error)} />}
        </Card>
      )}

      {isGenerated && (
        <Card className="flex flex-col gap-4">
          <div>
            <h3 className="font-medium text-gray-900 dark:text-white/90">Envoyer les convocations</h3>
            <p className="text-sm text-gray-500 dark:text-gray-400">
              Chaque canal a sa propre liste : un lot déjà joint par email reste à envoyer par les autres canaux, et
              aucun canal ne réexpédie ce qu'il a déjà fait partir.
            </p>
          </div>
          <div className="flex flex-wrap gap-3">
            {automatedChannels.map((channel) => {
              const pending = pendingCountOn(rows, channel.code);
              return (
                <Button
                  key={channel.code}
                  // react-query keeps the variables of the call in flight, which is how one
                  // button spins without freezing the others into a loading state they are not in.
                  isLoading={sendPending.isPending && sendPending.variables === channel.code}
                  disabled={pending === 0 || sendPending.isPending}
                  onClick={() => sendPending.mutate(channel.code)}
                >
                  {`Envoyer par ${channel.label.toLowerCase()}${pending > 0 ? ` (${pending})` : ''}`}
                </Button>
              );
            })}
            <Button variant="secondary" isLoading={remind.isPending} onClick={() => remind.mutate()}>
              Relancer les non-répondants
            </Button>
          </div>
          {automatedChannels.length === 0 && !channels.isLoading && (
            <Alert
              variant="warning"
              message="Aucun canal automatisé n'est actif : imprimez les convocations depuis leur détail, remettez-les, puis enregistrez la remise lot par lot."
            />
          )}
          {channels.isError && <Alert message={getErrorMessage(channels.error)} />}
          {sendPending.isError && <Alert message={getErrorMessage(sendPending.error)} />}
          {sendPending.isSuccess && (
            <Alert
              variant={sendPending.data.failedCount > 0 ? 'warning' : 'success'}
              message={
                sendPending.data.failedCount > 0
                  ? `${sendPending.data.sentCount} convocation(s) envoyée(s), ${sendPending.data.failedCount} en échec — les lots concernés apparaissent en « Échec » dans le suivi.`
                  : `${sendPending.data.sentCount} convocation(s) envoyée(s).`
              }
            />
          )}
          {remind.isError && <Alert message={getErrorMessage(remind.error)} />}
          {remind.isSuccess && (
            <Alert
              variant="success"
              message={`${remind.data.remindedCount} relance(s) envoyée(s) aux lots joignables restés sans réponse.`}
            />
          )}
        </Card>
      )}

      <Card className="flex flex-col gap-4">
        <div className="flex flex-wrap items-baseline justify-between gap-2">
          <h3 className="font-medium text-gray-900 dark:text-white/90">Suivi des convocations</h3>
          {isGenerated && canGenerate && (
            // Generating is idempotent - find-or-create by (assemblée, lot) - so this is not a
            // reset and cannot duplicate anything. It has exactly one use, and the label says
            // which: a lot added to the copropriété after the first run.
            <Button variant="secondary" isLoading={isGenerating} onClick={generateAll}>
              Générer les convocations des lots ajoutés depuis
            </Button>
          )}
        </div>
        {isGenerated && (generate.isError || schedule.isError) && (
          <Alert message={getErrorMessage(generate.error ?? schedule.error)} />
        )}

        {convocations.isLoading && <Loader label="Chargement des convocations…" />}
        {convocations.isError && <Alert message={getErrorMessage(convocations.error)} />}

        {convocations.data && !isGenerated && (
          <EmptyState title="Aucune convocation générée">
            Générez les convocations pour ouvrir le suivi lot par lot.
          </EmptyState>
        )}

        {isGenerated && (
          <>
            <FilterPanel
              activeCount={countActiveFilters(filters, DEFAULT_CONVOCATION_FILTERS, CONVOCATION_SORT_KEYS)}
              onClear={() => handleFiltersChange(DEFAULT_CONVOCATION_FILTERS)}
              search={{
                value: filters.search,
                onChange: (search) => handleFiltersChange({ ...filters, search }),
                placeholder: 'Rechercher un lot, un copropriétaire…',
              }}
            >
              <ConvocationFilters value={filters} channels={channelList} onChange={handleFiltersChange} />
            </FilterPanel>

            {visible.length === 0 ? (
              <EmptyState title="Aucun lot">Aucun lot ne correspond à ces critères.</EmptyState>
            ) : (
              <div className="flex flex-col gap-3">
                <p className="text-sm text-gray-500 dark:text-gray-400">
                  {visible.length} lot(s) sur {rows.length}
                </p>
                <ConvocationTrackingTable
                  convocations={pageRows}
                  sortBy={filters.sortBy}
                  sortDirection={filters.sortDirection}
                  onSort={handleSort}
                  detailPathOf={(convocationId) =>
                    `/property-mngt/properties/${property.id}/general-meetings/${meeting.id}/convocations/${convocationId}`
                  }
                />
                <Pagination pageNumber={currentPage} totalPages={totalPages} onPageChange={setPage} />
              </div>
            )}
          </>
        )}
      </Card>
    </div>
  );
}
