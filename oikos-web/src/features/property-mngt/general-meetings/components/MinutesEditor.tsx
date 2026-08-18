import { useState } from 'react';
import { QuillEditor } from '@/shared/components/RichText/QuillEditor';
import { Alert } from '@/shared/components/Alert/Alert';
import { Button } from '@/shared/components/Button/Button';
import { getErrorMessage } from '@/shared/utils/getErrorMessage';

interface MinutesEditorProps {
  initialContent: string;
  isSaving: boolean;
  isRegenerating: boolean;
  isValidating: boolean;
  error: unknown;
  onSave: (content: string) => void;
  onRegenerate: () => void;
  onValidate: () => void;
}

/**
 * The draft editor, holding the unsaved text.
 *
 * <p>Rich text, not a textarea of raw HTML. The draft is composed by the API
 * with headings, bold and lists in it, and a textarea showed the syndic
 * `<h1>Procès-verbal — …` as characters to be typed around. A procès-verbal is
 * a document the syndic completes with what was said in the room, and asking
 * for markup is asking the wrong person for the wrong thing.
 *
 * <p>Headings are turned on here and nowhere else: they are exactly what
 * sanitizeDocumentHtml lets through and sanitizeRichText does not, and this is
 * the one field displayed with the wider list.
 *
 * <p>Its own component rather than state inside the tab, so the parent can
 * reset it by remounting it (key on the server content) instead of syncing an
 * effect: regenerating the minutes has to throw the local buffer away, and
 * "reset state when a prop changes" is exactly what a key is for - which
 * matters more with Quill, since it owns its content after mount and pushing a
 * prop back into it mid-typing scrambles the text (see QuillEditor's comment).
 */
export function MinutesEditor({
  initialContent,
  isSaving,
  isRegenerating,
  isValidating,
  error,
  onSave,
  onRegenerate,
  onValidate,
}: MinutesEditorProps) {
  const [draft, setDraft] = useState(initialContent);

  return (
    <>
      <p className="text-sm text-gray-500 dark:text-gray-400">
        Complétez le brouillon : débats, remarques, opposition qu'un copropriétaire demande à faire consigner.
        Valider fige définitivement le texte.
      </p>
      <QuillEditor
        defaultValue={initialContent}
        onChange={setDraft}
        headings
        minHeight={420}
        ariaLabel="Contenu du procès-verbal"
      />
      {error != null && <Alert message={getErrorMessage(error)} />}
      <div className="flex flex-wrap gap-3">
        <Button isLoading={isSaving} onClick={() => onSave(draft)}>
          Enregistrer
        </Button>
        <Button variant="secondary" isLoading={isRegenerating} onClick={onRegenerate}>
          Régénérer depuis la séance
        </Button>
        <Button variant="secondary" isLoading={isValidating} onClick={onValidate}>
          Valider (fige le texte)
        </Button>
      </div>
      <p className="text-xs text-gray-400 dark:text-gray-500">
        Régénérer écrase les modifications manuelles et repart des données de la séance.
      </p>
    </>
  );
}
