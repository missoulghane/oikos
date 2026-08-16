import { useOutletContext } from 'react-router-dom';
import type { GeneralMeetingContext } from '@/features/property-mngt/general-meetings/pages/GeneralMeetingDetailLayout';
import { isNotFound, useMeetingMinutes } from '@/features/property-mngt/general-meetings/hooks/useMeetingMinutes';
import {
  useGenerateMeetingMinutes,
  usePublishMeetingMinutes,
  useUpdateMeetingMinutes,
  useValidateMeetingMinutes,
} from '@/features/property-mngt/general-meetings/hooks/useMeetingMutations';
import {
  MINUTES_STATUS_COLORS,
  MINUTES_STATUS_LABELS,
} from '@/features/property-mngt/general-meetings/constants/generalMeetingLabels';
import { MinutesEditor } from '@/features/property-mngt/general-meetings/components/MinutesEditor';
import { RichTextContent } from '@/shared/components/RichText/RichTextContent';
import { Alert } from '@/shared/components/Alert/Alert';
import { Badge } from '@/shared/components/Badge/Badge';
import { Button } from '@/shared/components/Button/Button';
import { Card } from '@/shared/components/Card/Card';
import { EmptyState } from '@/shared/components/EmptyState/EmptyState';
import { Loader } from '@/shared/components/Loader/Loader';
import { getErrorMessage } from '@/shared/utils/getErrorMessage';

export function MinutesTab() {
  const { meeting } = useOutletContext<GeneralMeetingContext>();
  const minutes = useMeetingMinutes(meeting.id);
  const generate = useGenerateMeetingMinutes(meeting.id);
  const update = useUpdateMeetingMinutes(meeting.id);
  const validate = useValidateMeetingMinutes(meeting.id);
  const publish = usePublishMeetingMinutes(meeting.id);

  const sessionClosed = meeting.status === 'CLOSED' || meeting.status === 'MINUTES_PUBLISHED';
  const noMinutesYet = minutes.isError && isNotFound(minutes.error);
  const isDraft = minutes.data?.status === 'DRAFT';

  return (
    <div className="flex flex-col gap-6">
      {!sessionClosed && (
        <Alert
          variant="warning"
          message="Le procès-verbal ne peut être rédigé qu'une fois la séance clôturée : il fige les présences et chaque scrutin."
        />
      )}

      {minutes.isLoading && <Loader label="Chargement du procès-verbal…" />}
      {minutes.isError && !noMinutesYet && <Alert message={getErrorMessage(minutes.error)} />}

      {noMinutesYet && (
        <Card>
          <EmptyState
            title="Aucun procès-verbal"
            action={
              <Button
                isLoading={generate.isPending}
                disabled={!sessionClosed}
                onClick={() => generate.mutate()}
              >
                Générer le brouillon
              </Button>
            }
          >
            Le brouillon reprend les présences, le quorum et le résultat de chaque scrutin.
          </EmptyState>
          {generate.isError && <Alert message={getErrorMessage(generate.error)} />}
        </Card>
      )}

      {minutes.data && (
        <Card className="flex flex-col gap-4">
          <div className="flex flex-wrap items-center justify-between gap-2">
            <h3 className="font-medium text-gray-900 dark:text-white/90">Procès-verbal</h3>
            <Badge color={MINUTES_STATUS_COLORS[minutes.data.status]}>
              {MINUTES_STATUS_LABELS[minutes.data.status]}
            </Badge>
          </div>

          {isDraft ? (
            // Keyed on the server content: regenerating throws the unsaved buffer away by
            // remounting the editor, rather than syncing it through an effect.
            <MinutesEditor
              key={minutes.data.content}
              initialContent={minutes.data.content}
              isSaving={update.isPending}
              isRegenerating={generate.isPending}
              isValidating={validate.isPending}
              error={update.error ?? generate.error ?? validate.error}
              onSave={(content) => update.mutate(content)}
              onRegenerate={() => generate.mutate()}
              onValidate={() => validate.mutate()}
            />
          ) : (
            // API-composed HTML, but the syndic edits the draft by hand before validating -
            // so what comes back is user input whatever produced it first, and it is
            // sanitized like any other. `document` keeps the headings the composer emits,
            // which the editor's narrower allow-list would strip.
            <RichTextContent html={minutes.data.content} variant="document" />
          )}

          {publish.isError && <Alert message={getErrorMessage(publish.error)} />}

          {minutes.data.status === 'UNDER_REVIEW' && (
            <div className="flex flex-col gap-2">
              <Alert
                variant="warning"
                message="La publication est irréversible : le procès-verbal sera diffusé à tous les copropriétaires."
              />
              <Button isLoading={publish.isPending} onClick={() => publish.mutate()}>
                Publier le procès-verbal
              </Button>
            </div>
          )}

          {minutes.data.status === 'PUBLISHED' && minutes.data.publishedAt && (
            <p className="text-sm text-gray-500 dark:text-gray-400">
              Publié le {new Date(minutes.data.publishedAt).toLocaleDateString('fr-FR')} · le PDF est disponible
              dans les documents de l'assemblée.
            </p>
          )}
        </Card>
      )}
    </div>
  );
}
