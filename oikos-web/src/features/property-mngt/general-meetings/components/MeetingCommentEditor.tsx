import { useRef, useState } from 'react';
import { QuillEditor, type QuillEditorHandle } from '@/shared/components/RichText/QuillEditor';
import { Alert } from '@/shared/components/Alert/Alert';
import { Button } from '@/shared/components/Button/Button';
import { getErrorMessage } from '@/shared/utils/getErrorMessage';

interface MeetingCommentEditorProps {
  initialComment: string;
  isSaving: boolean;
  error: unknown;
  onSave: (comment: string) => void;
}

/**
 * The meeting's note of intent, edited in place.
 *
 * <p>Its own component so the parent can reset it by remounting (key on the
 * server value) rather than syncing an effect - the same arrangement
 * MinutesEditor uses, and for the same reason: Quill owns its content after
 * mount and pushing a prop back into it mid-typing scrambles the text (see
 * QuillEditor's own comment).
 *
 * <p>"Effacer" writes an empty string rather than hiding the field: the API
 * normalises blank to null, so clearing the editor and saving is how a comment
 * is removed - there is no separate delete to build.
 */
export function MeetingCommentEditor({ initialComment, isSaving, error, onSave }: MeetingCommentEditorProps) {
  const editorRef = useRef<QuillEditorHandle>(null);
  const [draft, setDraft] = useState(initialComment);

  function clear() {
    editorRef.current?.setHtml('');
    setDraft('');
  }

  return (
    <div className="flex flex-col gap-3">
      <QuillEditor
        ref={editorRef}
        defaultValue={initialComment}
        onChange={setDraft}
        minHeight={160}
        ariaLabel="Commentaire de l'assemblée générale"
        placeholder="Contexte du vote, note d'intention, précisions à porter à la connaissance des copropriétaires…"
      />
      {error != null && <Alert message={getErrorMessage(error)} />}
      <div className="flex flex-wrap gap-2">
        <Button isLoading={isSaving} onClick={() => onSave(draft)}>
          Enregistrer le commentaire
        </Button>
        {draft !== '' && (
          <Button variant="secondary" disabled={isSaving} onClick={clear}>
            Effacer
          </Button>
        )}
      </div>
    </div>
  );
}
