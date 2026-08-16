import { useOutletContext } from 'react-router-dom';
import { canWriteDocuments, useCurrentUser } from '@/features/identity/me';
import { AttachmentsPanel } from '@/features/property-mngt/documents';
import type { GeneralMeetingContext } from '@/features/property-mngt/general-meetings/pages/GeneralMeetingDetailLayout';
import { MeetingCommentEditor } from '@/features/property-mngt/general-meetings/components/MeetingCommentEditor';
import { useUpdateGeneralMeetingComment } from '@/features/property-mngt/general-meetings/hooks/useMeetingMutations';
import { Card } from '@/shared/components/Card/Card';

/**
 * What concerns the assembly as a whole rather than one point of its agenda:
 * the syndic's note of intent, and the documents that go with it.
 *
 * <p>Both are read by the copropriétaires - in their own space, on mobile, and
 * the comment again in the convocation letter. That is what the wording under
 * each heading has to make obvious, because a syndic writing a private
 * reminder here would be writing it to the whole copropriété.
 */
export function MeetingInformationTab() {
  const { property, meeting } = useOutletContext<GeneralMeetingContext>();
  const currentUser = useCurrentUser();
  const canWrite = currentUser.data ? canWriteDocuments(currentUser.data, property.id) : false;
  const updateComment = useUpdateGeneralMeetingComment(meeting.id);

  return (
    <div className="flex flex-col gap-6">
      <Card className="flex flex-col gap-4">
        <div>
          <h3 className="font-medium text-gray-900 dark:text-white/90">Commentaire</h3>
          <p className="text-sm text-gray-500 dark:text-gray-400">
            Facultatif. <strong>Lu par les copropriétaires</strong> : il apparaît dans leur espace, sur mobile, et
            dans la convocation qu'ils reçoivent. Ce n'est pas une note interne.
          </p>
        </div>
        {/* Remounted on the server value: regenerating the editor's content from a prop
            while someone is typing in it scrambles the text (see QuillEditor). */}
        <MeetingCommentEditor
          key={meeting.comment ?? ''}
          initialComment={meeting.comment ?? ''}
          isSaving={updateComment.isPending}
          error={updateComment.isError ? updateComment.error : null}
          onSave={(comment) => updateComment.mutate(comment)}
        />
      </Card>

      <Card className="flex flex-col gap-4">
        <div>
          <h3 className="font-medium text-gray-900 dark:text-white/90">Documents de l'assemblée</h3>
          <p className="text-sm text-gray-500 dark:text-gray-400">
            Les pièces qui éclairent l'assemblée entière — budget, rapport de gestion, devis. Celles qui portent sur
            un point précis se joignent à ce point, dans l'onglet « Ordre du jour ».
          </p>
        </div>
        <AttachmentsPanel
          ownerType="GENERAL_MEETING"
          ownerId={meeting.id}
          canWrite={canWrite}
          emptyLabel="Aucun document joint à cette assemblée."
        />
      </Card>
    </div>
  );
}
